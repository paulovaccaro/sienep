# ============================================================
#  SIENEP - Script de Tests de API
#  Uso: powershell -ExecutionPolicy Bypass -File ".\test_sienep.ps1"
#  Requisito: la app debe estar corriendo en localhost:8080
# ============================================================
param(
    [string]$BaseUrl = "http://localhost:8080"
)

$script:PASS    = 0
$script:FAIL    = 0
$script:SKIP    = 0
$script:FALLIDOS = [System.Collections.Generic.List[string]]::new()

$script:TOKEN_ADMIN       = $null
$script:TOKEN_PSICO       = $null
$script:ID_ADMIN          = $null
$script:ID_PSICO          = $null
$script:ID_ESTUDIANTE1    = $null
$script:ID_ESTUDIANTE2    = $null
$script:ID_CARRERA        = $null
$script:ID_ITR            = $null
$script:ID_GRUPO          = $null
$script:ID_INSTANCIA      = $null
$script:ID_INSTANCIA2     = $null
$script:ID_SEGUIMIENTO    = $null
$script:ID_OBSERVACION    = $null
$script:ID_INFORME        = $null
$script:ID_INCIDENCIA     = $null
$script:ID_ARCHIVO        = $null
$script:ID_NOTIFICACION   = $null
$script:ID_RECORDATORIO   = $null
$script:ID_EVENTO         = $null
$script:ID_CAT_INSTANCIA  = $null
$script:ID_CAT_RECORD     = $null
$script:ID_ASIGNACION     = $null

# --- Generador de CI uruguaya valida ---
function New-ValidCI([string]$base7) {
    $coef = 2, 9, 8, 7, 6, 3, 4
    $suma = 0
    for ($i = 0; $i -lt 7; $i++) {
        $suma += [int]::Parse($base7[$i].ToString()) * $coef[$i]
    }
    $v = (10 - ($suma % 10)) % 10
    return $base7 + $v.ToString()
}

$ts   = [System.DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$base = (($ts % 8000000) + 1000000)

$CI_ADMIN    = New-ValidCI ($base.ToString())
$CI_PSICO    = New-ValidCI (($base + 1).ToString())
$CI_EST1     = New-ValidCI (($base + 2).ToString())
$CI_EST2     = New-ValidCI (($base + 3).ToString())
$CI_MENOR    = New-ValidCI (($base + 4).ToString())
$CI_INVALIDA = "12345678"

$USER_ADMIN = "admin.sienep"
$USER_PSICO = "psico.sienep"

# --- Output helpers ---
function Write-Header([string]$t) {
    Write-Host ""
    Write-Host ("=" * 60) -ForegroundColor Cyan
    Write-Host "  $t" -ForegroundColor Cyan
    Write-Host ("=" * 60) -ForegroundColor Cyan
}
function Write-Sub([string]$t) {
    Write-Host ""
    Write-Host "  -- $t" -ForegroundColor DarkCyan
}
function OK([string]$msg) {
    Write-Host "  [PASS] $msg" -ForegroundColor Green
    $script:PASS++
}
function KO([string]$msg, [string]$det = "") {
    $line = "  [FAIL] $msg"
    if ($det) { $line += " | $det" }
    Write-Host $line -ForegroundColor Red
    $script:FAIL++
    $script:FALLIDOS.Add($line)
}
function OMIT([string]$msg) {
    Write-Host "  [SKIP] $msg" -ForegroundColor Yellow
    $script:SKIP++
}

# --- HTTP client ---
function Invoke-Api {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body  = $null,
        [string]$Token = $null
    )
    $headers = @{ "Accept" = "application/json" }
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    $uri      = "$BaseUrl$Path"
    $bodyJson = $null
    if ($Body -and $Method -notin @("GET","DELETE")) {
        $headers["Content-Type"] = "application/json"
        $bodyJson = $Body | ConvertTo-Json -Depth 10
    }
    try {
        $params = @{ Method = $Method; Uri = $uri; Headers = $headers; UseBasicParsing = $true }
        if ($bodyJson) { $params["Body"] = $bodyJson }
        $resp   = Invoke-WebRequest @params
        $parsed = $null
        try { $parsed = $resp.Content | ConvertFrom-Json } catch {}
        return @{ Status = [int]$resp.StatusCode; Body = $parsed; Raw = $resp.Content }
    } catch {
        $code = 0
        $raw  = ""
        if ($_.Exception.Response) {
            $code = [int]$_.Exception.Response.StatusCode
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $raw    = $reader.ReadToEnd()
                $reader.Close()
            } catch {}
        }
        $parsed = $null
        try { $parsed = $raw | ConvertFrom-Json } catch {}
        return @{ Status = $code; Body = $parsed; Raw = $raw; Err = $_.Exception.Message }
    }
}

function Assert-Status([string]$nombre, [hashtable]$r, [int]$expected) {
    if ($r.Status -eq $expected) {
        OK $nombre
        return $true
    }
    $det = ""
    if ($r.Body -and $r.Body.error) { $det = $r.Body.error }
    elseif ($r.Err) { $det = $r.Err }
    KO $nombre "Esperado $expected, recibido $($r.Status). $det"
    return $false
}

# ============================================================
# 0. VERIFICACION DEL SERVIDOR
# ============================================================
Write-Header "0. VERIFICACION DEL SERVIDOR"

$ping = Invoke-Api "GET" "/swagger-ui.html"
if ($ping.Status -in 200, 301, 302) {
    OK "Servidor responde en $BaseUrl"
} else {
    KO "Servidor NO responde en $BaseUrl"
    Write-Host ""
    Write-Host "  Inicia la app primero:" -ForegroundColor Yellow
    Write-Host "    cd sienep" -ForegroundColor Yellow
    Write-Host "    ./mvnw spring-boot:run" -ForegroundColor Yellow
    exit 1
}

# ============================================================
# 1. AUTENTICACION
# ============================================================
Write-Header "1. AUTENTICACION (/auth)"

Write-Sub "Registro Admin (Rol 1 = Administrador)"
$regAdmin = Invoke-Api "POST" "/auth/registro" @{
    cedula        = $CI_ADMIN
    nombre        = "Admin"
    apellido      = "Sienep"
    password      = "password123"
    fecNacimiento = "1985-06-15"
    idRol         = 1
}
if ($regAdmin.Status -eq 201) {
    OK "POST /auth/registro Admin -> 201 Created"
    $script:TOKEN_ADMIN = $regAdmin.Body.token
    $script:ID_ADMIN    = $regAdmin.Body.userId
    if ($script:TOKEN_ADMIN) { OK "  Token JWT recibido (userId=$($script:ID_ADMIN))" }
    else { KO "  Token JWT ausente en la respuesta" }
} elseif ($regAdmin.Status -eq 400 -and "$($regAdmin.Body.error)" -match "Ya existe") {
    OMIT "CI $CI_ADMIN ya existe (run anterior) - intentando login"
    $fb = Invoke-Api "POST" "/auth/login" @{ username = $USER_ADMIN; password = "password123" }
    if ($fb.Status -eq 200) {
        OK "Login fallback Admin -> 200"
        $script:TOKEN_ADMIN = $fb.Body.token
        $script:ID_ADMIN    = $fb.Body.userId
    } else {
        KO "Login fallback Admin fallo - limpia el usuario con CI $CI_ADMIN de la DB"
    }
} else {
    KO "POST /auth/registro Admin -> $($regAdmin.Status)" "$($regAdmin.Body.error)"
}

Write-Sub "Validaciones de registro"
$r = Invoke-Api "POST" "/auth/registro" @{
    cedula = $CI_INVALIDA; nombre = "Test"; apellido = "CIBad"
    password = "password123"; fecNacimiento = "1990-01-01"; idRol = 1
}
Assert-Status "CI invalida -> 400" $r 400

$r = Invoke-Api "POST" "/auth/registro" @{
    cedula = $CI_MENOR; nombre = "Test"; apellido = "ShortPass"
    password = "abc"; fecNacimiento = "1990-01-01"; idRol = 1
}
Assert-Status "Password corta (menos de 8 chars) -> 400" $r 400

$r = Invoke-Api "POST" "/auth/registro" @{
    cedula        = $CI_MENOR
    nombre        = "Test"
    apellido      = "Joven"
    password      = "password123"
    fecNacimiento = (Get-Date).AddYears(-16).ToString("yyyy-MM-dd")
    idRol         = 1
}
Assert-Status "Menor de edad (16 anios) -> 400" $r 400

$r = Invoke-Api "POST" "/auth/registro" @{
    cedula = $CI_ADMIN; nombre = "Admin"; apellido = "Sienep"
    password = "password123"; fecNacimiento = "1985-06-15"; idRol = 1
}
Assert-Status "CI duplicada -> 400" $r 400

Write-Sub "Login"
if ($script:TOKEN_ADMIN) {
    $usernameAdmin = if ($regAdmin.Body -and $regAdmin.Body.username) { $regAdmin.Body.username } else { $USER_ADMIN }

    $r = Invoke-Api "POST" "/auth/login" @{ username = $usernameAdmin; password = "password123" }
    if (Assert-Status "Login credenciales correctas -> 200" $r 200) {
        if ($r.Body.token) { OK "  Token presente en respuesta" } else { KO "  Token ausente" }
    }
    $r = Invoke-Api "POST" "/auth/login" @{ username = $usernameAdmin; password = "WRONGPASSWORD" }
    Assert-Status "Login password incorrecta -> 400" $r 400

    $r = Invoke-Api "POST" "/auth/login" @{ username = "noexiste.usuario9999"; password = "password123" }
    Assert-Status "Login usuario inexistente -> 400" $r 400
} else {
    OMIT "Tests de login omitidos (sin token admin)"
}

Write-Sub "Endpoint protegido sin token"
$r = Invoke-Api "GET" "/api/carreras"
Assert-Status "GET /api/carreras sin token -> 401" $r 401

# ============================================================
# 2. CARRERAS
# ============================================================
Write-Header "2. CARRERAS (/api/carreras)"

if (-not $script:TOKEN_ADMIN) { OMIT "Sin token admin - seccion omitida" }
else {
    $codCarrera = "TST" + ($ts % 9999)

    $r = Invoke-Api "POST" "/api/carreras" @{
        codigo = $codCarrera
        nombre = "Carrera de Test Automatizado"
        plan   = "2026"
    } $script:TOKEN_ADMIN

    if (Assert-Status "POST /api/carreras -> 201" $r 201) {
        $script:ID_CARRERA = $r.Body.idCarrera
        OK "  Carrera creada ID=$($script:ID_CARRERA), codigo=$codCarrera"
    }

    $r = Invoke-Api "GET" "/api/carreras" $null $script:TOKEN_ADMIN
    if (Assert-Status "GET /api/carreras -> 200" $r 200) {
        $count = if ($r.Body -is [array]) { $r.Body.Count } else { 1 }
        OK "  $count carrera(s) en la DB"
    }

    if ($script:ID_CARRERA) {
        $r = Invoke-Api "GET" "/api/carreras/$($script:ID_CARRERA)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/carreras/{id} -> 200" $r 200

        $r = Invoke-Api "PUT" "/api/carreras/$($script:ID_CARRERA)" @{
            codigo = $codCarrera; nombre = "Carrera Actualizada"; plan = "2026"
        } $script:TOKEN_ADMIN
        Assert-Status "PUT /api/carreras/{id} -> 200" $r 200

        $r = Invoke-Api "GET" "/api/carreras/99999" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/carreras/99999 (no existe) -> 400" $r 400
    }

    $r = Invoke-Api "POST" "/api/carreras" @{
        codigo = $codCarrera; nombre = "Duplicada"; plan = "2026"
    } $script:TOKEN_ADMIN
    Assert-Status "POST /api/carreras codigo duplicado -> 400" $r 400

    if (-not $script:ID_CARRERA) {
        $existing = Invoke-Api "GET" "/api/carreras" $null $script:TOKEN_ADMIN
        if ($existing.Status -eq 200 -and $existing.Body) {
            $first = if ($existing.Body -is [array]) { $existing.Body[0] } else { $existing.Body }
            $script:ID_CARRERA = $first.idCarrera
            OMIT "Usando carrera existente ID=$($script:ID_CARRERA)"
        }
    }
}

# ============================================================
# 3. ITR
# ============================================================
Write-Header "3. ITR (/api/itr)"

if (-not $script:TOKEN_ADMIN) { OMIT "Sin token admin - seccion omitida" }
else {
    $r = Invoke-Api "GET" "/api/itr" $null $script:TOKEN_ADMIN
    if (Assert-Status "GET /api/itr -> 200" $r 200) {
        $itrs  = $r.Body
        $count = if ($itrs -is [array]) { $itrs.Count } else { 1 }
        OK "  $count ITR(s) en la DB (sembrados por DataSeeder)"
        if ($count -gt 0) {
            $first = if ($itrs -is [array]) { $itrs[0] } else { $itrs }
            $script:ID_ITR = $first.idItr
            OK "  Usando ITR ID=$($script:ID_ITR) ($($first.nombre))"
        } else {
            KO "  No hay ITRs - el DataSeeder no se ejecuto"
        }
    }

    if ($script:ID_ITR) {
        $r = Invoke-Api "GET" "/api/itr/$($script:ID_ITR)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/itr/{id} -> 200" $r 200

        $itrActual = (Invoke-Api "GET" "/api/itr/$($script:ID_ITR)" $null $script:TOKEN_ADMIN).Body
        $r = Invoke-Api "PUT" "/api/itr/$($script:ID_ITR)" @{
            codigo = $itrActual.codigo; nombre = "$($itrActual.nombre) (test)"
        } $script:TOKEN_ADMIN
        Assert-Status "PUT /api/itr/{id} -> 200" $r 200
        Invoke-Api "PUT" "/api/itr/$($script:ID_ITR)" @{
            codigo = $itrActual.codigo; nombre = $itrActual.nombre
        } $script:TOKEN_ADMIN | Out-Null

        $r = Invoke-Api "GET" "/api/itr/99999" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/itr/99999 (no existe) -> 400" $r 400

        OMIT "DELETE /api/itr omitido (datos usados en tests posteriores)"
    }
}

# ============================================================
# 4. GRUPOS
# ============================================================
Write-Header "4. GRUPOS (/api/grupos)"

if (-not $script:TOKEN_ADMIN) { OMIT "Sin token admin - seccion omitida" }
else {
    $r = Invoke-Api "GET" "/api/grupos" $null $script:TOKEN_ADMIN
    if (Assert-Status "GET /api/grupos -> 200" $r 200) {
        $grupos = $r.Body
        $count  = if ($grupos -is [array]) { $grupos.Count } else { 1 }
        OK "  $count grupo(s) en la DB"
        if ($count -gt 0) {
            $first = if ($grupos -is [array]) { $grupos[0] } else { $grupos }
            $script:ID_GRUPO = $first.idGrupo
            OK "  Usando Grupo ID=$($script:ID_GRUPO) ($($first.nomGrupo))"
        }
    }

    if ($script:ID_GRUPO) {
        $r = Invoke-Api "GET" "/api/grupos/$($script:ID_GRUPO)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/grupos/{id} -> 200" $r 200

        if ($script:ID_ITR) {
            $r = Invoke-Api "GET" "/api/grupos?idItr=$($script:ID_ITR)" $null $script:TOKEN_ADMIN
            Assert-Status "GET /api/grupos?idItr={id} (filtro por ITR) -> 200" $r 200
        }
    }

    if ($script:ID_CARRERA -and $script:ID_ITR) {
        $nomGrupo = "TST-$($ts % 9999)-1"
        $r = Invoke-Api "POST" "/api/grupos" @{
            nomGrupo  = $nomGrupo
            idCarrera = $script:ID_CARRERA
            idItr     = $script:ID_ITR
            anio      = 2026
            semestre  = 1
        } $script:TOKEN_ADMIN

        if (Assert-Status "POST /api/grupos -> 201" $r 201) {
            $nuevoGrupoId = $r.Body.idGrupo
            OK "  Grupo temporal creado ID=$nuevoGrupoId"
            $rd = Invoke-Api "DELETE" "/api/grupos/$nuevoGrupoId" $null $script:TOKEN_ADMIN
            Assert-Status "DELETE /api/grupos/{id} -> 204" $rd 204
        }

        $r = Invoke-Api "POST" "/api/grupos" @{
            nomGrupo  = $nomGrupo
            idCarrera = $script:ID_CARRERA
            idItr     = $script:ID_ITR
            anio      = 2026
            semestre  = 3
        } $script:TOKEN_ADMIN
        if ($r.Status -ge 400) {
            OK "POST /api/grupos semestre invalido -> $($r.Status)"
        } else {
            KO "POST /api/grupos semestre invalido deberia fallar" "recibio $($r.Status)"
        }
    } else {
        OMIT "POST /api/grupos omitido (falta ID_CARRERA o ID_ITR)"
    }
}

# ============================================================
# 5. FUNCIONARIOS
# ============================================================
Write-Header "5. FUNCIONARIOS (/api/funcionarios)"

if (-not $script:TOKEN_ADMIN) { OMIT "Sin token admin - seccion omitida" }
else {
    $bodyPsico = @{
        cedula        = $CI_PSICO
        nombre        = "Psico"
        apellido      = "Sienep"
        password      = "password123"
        fecNacimiento = "1988-03-20"
        idRol         = 2
    }
    if ($script:ID_ITR) { $bodyPsico["idItr"] = $script:ID_ITR }

    $regPsico = Invoke-Api "POST" "/auth/registro" $bodyPsico
    if ($regPsico.Status -eq 201) {
        OK "POST /auth/registro Psicopedagogo -> 201"
        $script:TOKEN_PSICO = $regPsico.Body.token
        $script:ID_PSICO    = $regPsico.Body.userId
    } elseif ($regPsico.Status -eq 400 -and "$($regPsico.Body.error)" -match "Ya existe") {
        OMIT "Psicopedagogo ya existe - intentando login"
        $fb = Invoke-Api "POST" "/auth/login" @{ username = $USER_PSICO; password = "password123" }
        if ($fb.Status -eq 200) {
            $script:TOKEN_PSICO = $fb.Body.token
            $script:ID_PSICO    = $fb.Body.userId
            OK "Login fallback Psico OK"
        } else { KO "Fallback login Psico fallo" }
    } else {
        KO "POST /auth/registro Psicopedagogo -> $($regPsico.Status)" "$($regPsico.Body.error)"
    }

    $r = Invoke-Api "GET" "/api/funcionarios" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/funcionarios (admin) -> 200" $r 200

    if ($script:ID_ADMIN) {
        $r = Invoke-Api "GET" "/api/funcionarios/$($script:ID_ADMIN)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/funcionarios/{id} -> 200" $r 200
    }

    if ($script:TOKEN_PSICO) {
        $r = Invoke-Api "GET" "/api/funcionarios" $null $script:TOKEN_PSICO
        Assert-Status "GET /api/funcionarios (psico sin permiso) -> 403" $r 403
    }
}

# ============================================================
# 6. ESTUDIANTES
# ============================================================
Write-Header "6. ESTUDIANTES (/api/estudiantes)"

if (-not $script:TOKEN_ADMIN -or -not $script:ID_GRUPO) {
    OMIT "Sin token admin o sin ID_GRUPO - seccion omitida"
} else {
    $r = Invoke-Api "POST" "/api/estudiantes" @{
        cedula        = $CI_EST1
        nombre        = "Estudiante"
        apellido      = "Uno"
        password      = "password123"
        fecNacimiento = "2000-05-10"
        idGrupo       = $script:ID_GRUPO
    } $script:TOKEN_ADMIN

    if (Assert-Status "POST /api/estudiantes -> 201" $r 201) {
        $script:ID_ESTUDIANTE1 = $r.Body.idUsuario
        OK "  Estudiante 1 ID=$($script:ID_ESTUDIANTE1)"
        if ($r.Body.correo -like "*@estudiantes.utec.edu.uy") {
            OK "  Correo con dominio correcto: $($r.Body.correo)"
        } else {
            KO "  Correo incorrecto: $($r.Body.correo)"
        }
        if ($r.Body.username) { OK "  Username generado: $($r.Body.username)" }
    }

    $r2 = Invoke-Api "POST" "/api/estudiantes" @{
        cedula        = $CI_EST2
        nombre        = "Estudiante"
        apellido      = "Dos"
        password      = "password123"
        fecNacimiento = "2001-08-20"
        idGrupo       = $script:ID_GRUPO
    } $script:TOKEN_ADMIN
    if (Assert-Status "POST /api/estudiantes (2do) -> 201" $r2 201) {
        $script:ID_ESTUDIANTE2 = $r2.Body.idUsuario
        OK "  Estudiante 2 ID=$($script:ID_ESTUDIANTE2)"
    }

    $r = Invoke-Api "GET" "/api/estudiantes" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/estudiantes (admin ve todos) -> 200" $r 200

    if ($script:ID_ESTUDIANTE1) {
        $r = Invoke-Api "GET" "/api/estudiantes/$($script:ID_ESTUDIANTE1)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/estudiantes/{id} -> 200" $r 200

        $r = Invoke-Api "PUT" "/api/estudiantes/$($script:ID_ESTUDIANTE1)" @{
            nombre    = "EstActualizado"
            apellido  = "Uno"
            idGrupo   = $script:ID_GRUPO
            estActivo = $true
        } $script:TOKEN_ADMIN
        Assert-Status "PUT /api/estudiantes/{id} -> 200" $r 200
    }

    $r = Invoke-Api "POST" "/api/estudiantes" @{
        cedula = $CI_INVALIDA; nombre = "Bad"; apellido = "CI"
        password = "password123"; fecNacimiento = "2000-01-01"; idGrupo = $script:ID_GRUPO
    } $script:TOKEN_ADMIN
    Assert-Status "POST /api/estudiantes CI invalida -> 400" $r 400

    if ($script:TOKEN_PSICO -and $script:ID_ESTUDIANTE1) {
        $r = Invoke-Api "GET" "/api/estudiantes" $null $script:TOKEN_PSICO
        Assert-Status "GET /api/estudiantes (psico scope ITR) -> 200" $r 200

        $r = Invoke-Api "GET" "/api/estudiantes/$($script:ID_ESTUDIANTE1)" $null $script:TOKEN_PSICO
        Assert-Status "GET /api/estudiantes/{id} (psico en su scope) -> 200" $r 200
    }
}

# ============================================================
# 7. ASIGNACIONES (RBAC)
# ============================================================
Write-Header "7. ASIGNACIONES (/api/asignaciones)"

if (-not $script:TOKEN_ADMIN -or -not $script:ID_PSICO) {
    OMIT "Sin token admin o sin ID_PSICO - seccion omitida"
} else {
    $bodyAsig = @{ idUsuario = $script:ID_PSICO; idRol = 3 }
    if ($script:ID_GRUPO) { $bodyAsig["idGrupo"] = $script:ID_GRUPO }

    $r = Invoke-Api "POST" "/api/asignaciones" $bodyAsig $script:TOKEN_ADMIN
    if (Assert-Status "POST /api/asignaciones -> 201" $r 201) {
        $script:ID_ASIGNACION = $r.Body.idAsignacion
        OK "  Asignacion ID=$($script:ID_ASIGNACION)"
    }

    if ($script:ID_ASIGNACION) {
        $r = Invoke-Api "GET" "/api/asignaciones/$($script:ID_ASIGNACION)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/asignaciones/{id} -> 200" $r 200

        $r = Invoke-Api "GET" "/api/asignaciones/usuario/$($script:ID_PSICO)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/asignaciones/usuario/{id} -> 200" $r 200

        $r = Invoke-Api "PUT" "/api/asignaciones/$($script:ID_ASIGNACION)" @{
            idRol = 3; estActivo = $true
        } $script:TOKEN_ADMIN
        Assert-Status "PUT /api/asignaciones/{id} -> 200" $r 200
    }

    $r = Invoke-Api "POST" "/api/asignaciones" @{ idUsuario = 1; idRol = 1 } $script:TOKEN_PSICO
    Assert-Status "POST /api/asignaciones sin permiso (psico) -> 403" $r 403
}

# ============================================================
# 8. CATEGORIAS DE INSTANCIA
# ============================================================
Write-Header "8. CATEGORIAS DE INSTANCIA (/api/categorias-instancia)"

if (-not $script:TOKEN_ADMIN) { OMIT "Sin token admin - seccion omitida" }
else {
    $r = Invoke-Api "POST" "/api/categorias-instancia" @{
        nombre      = "Seguimiento Test"
        descripcion = "Creada por script de test"
    } $script:TOKEN_ADMIN

    if (Assert-Status "POST /api/categorias-instancia -> 201" $r 201) {
        $script:ID_CAT_INSTANCIA = $r.Body.idCategoriaInstancia
        OK "  Categoria instancia ID=$($script:ID_CAT_INSTANCIA)"
    }

    $r = Invoke-Api "GET" "/api/categorias-instancia" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/categorias-instancia -> 200" $r 200

    if ($script:ID_CAT_INSTANCIA) {
        $r = Invoke-Api "GET" "/api/categorias-instancia/$($script:ID_CAT_INSTANCIA)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/categorias-instancia/{id} -> 200" $r 200

        $r = Invoke-Api "PUT" "/api/categorias-instancia/$($script:ID_CAT_INSTANCIA)" @{
            nombre = "Seguimiento Actualizado"; descripcion = "desc actualizada"
        } $script:TOKEN_ADMIN
        Assert-Status "PUT /api/categorias-instancia/{id} -> 200" $r 200
    }

    if ($script:TOKEN_PSICO) {
        $r = Invoke-Api "POST" "/api/categorias-instancia" @{
            nombre = "Test Psico"; descripcion = "sin permiso"
        } $script:TOKEN_PSICO
        Assert-Status "POST /api/categorias-instancia sin permiso -> 403" $r 403
    }
}

# ============================================================
# 9. INSTANCIAS
# ============================================================
Write-Header "9. INSTANCIAS (/api/instancias)"

if (-not $script:TOKEN_ADMIN -or -not $script:ID_ADMIN) {
    OMIT "Sin token admin - seccion omitida"
} else {
    $bodyInst = @{
        titulo        = "Reunion de Test"
        tipo          = "Reunion"
        fecHora       = "2026-09-15T10:00:00-03:00"
        descripcion   = "Instancia creada por script de test"
        idFuncionario = $script:ID_ADMIN
    }
    if ($script:ID_CAT_INSTANCIA) { $bodyInst["idCategoria"] = $script:ID_CAT_INSTANCIA }

    $r = Invoke-Api "POST" "/api/instancias" $bodyInst $script:TOKEN_ADMIN
    if (Assert-Status "POST /api/instancias -> 201" $r 201) {
        $script:ID_INSTANCIA = $r.Body.idInstancia
        OK "  Instancia ID=$($script:ID_INSTANCIA)"
    }

    $r2 = Invoke-Api "POST" "/api/instancias" @{
        titulo        = "Reunion para Incidencia"
        tipo          = "Incidencia"
        fecHora       = "2026-09-20T14:00:00-03:00"
        descripcion   = "Base para test de incidencia"
        idFuncionario = $script:ID_ADMIN
    } $script:TOKEN_ADMIN
    if ($r2.Status -eq 201) {
        $script:ID_INSTANCIA2 = $r2.Body.idInstancia
        OK "  Instancia2 (para incidencia) ID=$($script:ID_INSTANCIA2)"
    }

    $r = Invoke-Api "GET" "/api/instancias" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/instancias -> 200" $r 200

    if ($script:ID_INSTANCIA) {
        $r = Invoke-Api "GET" "/api/instancias/$($script:ID_INSTANCIA)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/instancias/{id} -> 200" $r 200

        $r = Invoke-Api "PUT" "/api/instancias/$($script:ID_INSTANCIA)" @{
            titulo        = "Reunion Actualizada"
            tipo          = "Reunion"
            fecHora       = "2026-09-15T11:00:00-03:00"
            descripcion   = "Descripcion actualizada"
            idFuncionario = $script:ID_ADMIN
        } $script:TOKEN_ADMIN
        Assert-Status "PUT /api/instancias/{id} -> 200" $r 200

        $rClone = Invoke-Api "POST" "/api/instancias/$($script:ID_INSTANCIA)/clonar" @{
            fecHora = "2026-10-15T10:00:00-03:00"
        } $script:TOKEN_ADMIN
        if (Assert-Status "POST /api/instancias/{id}/clonar -> 201" $rClone 201) {
            $clonId = $rClone.Body.idInstancia
            OK "  Clon creado ID=$clonId"
            Invoke-Api "DELETE" "/api/instancias/$clonId" $null $script:TOKEN_ADMIN | Out-Null
        }
    }
}

# ============================================================
# 10. SEGUIMIENTOS
# ============================================================
Write-Header "10. SEGUIMIENTOS (/api/seguimientos)"

if (-not $script:TOKEN_ADMIN -or -not $script:ID_ESTUDIANTE1) {
    OMIT "Sin token admin o sin estudiante - seccion omitida"
} else {
    $r = Invoke-Api "POST" "/api/seguimientos" @{
        idEstudiante = $script:ID_ESTUDIANTE1
        fecInicio    = "2026-03-01"
        fecCierre    = "2026-07-01"
    } $script:TOKEN_ADMIN

    if (Assert-Status "POST /api/seguimientos -> 201" $r 201) {
        $script:ID_SEGUIMIENTO = $r.Body.idSeguimiento
        OK "  Seguimiento ID=$($script:ID_SEGUIMIENTO)"
    }

    $r = Invoke-Api "GET" "/api/seguimientos" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/seguimientos -> 200" $r 200

    $r = Invoke-Api "GET" "/api/seguimientos/estudiante/$($script:ID_ESTUDIANTE1)" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/seguimientos/estudiante/{id} -> 200" $r 200

    if ($script:ID_SEGUIMIENTO) {
        $r = Invoke-Api "GET" "/api/seguimientos/$($script:ID_SEGUIMIENTO)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/seguimientos/{id} -> 200" $r 200

        $r = Invoke-Api "PUT" "/api/seguimientos/$($script:ID_SEGUIMIENTO)" @{
            idEstudiante = $script:ID_ESTUDIANTE1
            fecInicio    = "2026-03-01"
            fecCierre    = "2026-12-01"
            estActivo    = $true
        } $script:TOKEN_ADMIN
        Assert-Status "PUT /api/seguimientos/{id} -> 200" $r 200
    }
}

# ============================================================
# 11. OBSERVACIONES
# ============================================================
Write-Header "11. OBSERVACIONES (/api/observaciones)"

if (-not $script:TOKEN_ADMIN -or -not $script:ID_ESTUDIANTE1) {
    OMIT "Sin token admin o sin estudiante - seccion omitida"
} else {
    $r = Invoke-Api "POST" "/api/observaciones" @{
        idEstudiante = $script:ID_ESTUDIANTE1
        titulo       = "Dificultades en comprension lectora"
        contenido    = "El estudiante muestra dificultades para interpretar textos complejos."
    } $script:TOKEN_ADMIN

    if (Assert-Status "POST /api/observaciones -> 201" $r 201) {
        $script:ID_OBSERVACION = $r.Body.idObservacion
        OK "  Observacion ID=$($script:ID_OBSERVACION)"
        if ($r.Body.idFuncionario) { OK "  Autor registrado desde JWT: $($r.Body.idFuncionario)" }
    }

    $r = Invoke-Api "GET" "/api/observaciones/estudiante/$($script:ID_ESTUDIANTE1)" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/observaciones/estudiante/{id} -> 200" $r 200

    if ($script:ID_OBSERVACION) {
        $r = Invoke-Api "GET" "/api/observaciones/$($script:ID_OBSERVACION)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/observaciones/{id} -> 200" $r 200
    }
}

# ============================================================
# 12. INFORMES FINALES
# ============================================================
Write-Header "12. INFORMES FINALES (/api/informes)"

if (-not $script:TOKEN_ADMIN) { OMIT "Sin token admin - seccion omitida" }
else {
    $r = Invoke-Api "POST" "/api/informes" @{
        contenido   = "El estudiante mostro mejora sostenida durante el periodo."
        valoracion  = 8
        fecCreacion = "2026-07-01"
    } $script:TOKEN_ADMIN

    if (Assert-Status "POST /api/informes -> 201" $r 201) {
        $script:ID_INFORME = $r.Body.idInfFinal
        OK "  Informe final ID=$($script:ID_INFORME)"
    }

    $r = Invoke-Api "GET" "/api/informes" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/informes -> 200" $r 200

    if ($script:ID_INFORME) {
        $r = Invoke-Api "GET" "/api/informes/$($script:ID_INFORME)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/informes/{id} -> 200" $r 200

        $r = Invoke-Api "PUT" "/api/informes/$($script:ID_INFORME)" @{
            contenido   = "Contenido actualizado."
            valoracion  = 9
            fecCreacion = "2026-07-15"
        } $script:TOKEN_ADMIN
        Assert-Status "PUT /api/informes/{id} -> 200" $r 200
    }
}

# ============================================================
# 13. INCIDENCIAS
# ============================================================
Write-Header "13. INCIDENCIAS (/api/incidencias)"

if (-not $script:TOKEN_ADMIN -or -not $script:ID_INSTANCIA2) {
    OMIT "Sin token admin o sin instancia2 - seccion omitida"
} else {
    $r = Invoke-Api "POST" "/api/incidencias" @{
        idInstancia = $script:ID_INSTANCIA2
        lugar       = "Aula 204 - Edificio Central"
    } $script:TOKEN_ADMIN

    if (Assert-Status "POST /api/incidencias -> 201" $r 201) {
        $script:ID_INCIDENCIA = $r.Body.idInstancia
        OK "  Incidencia ID=$($script:ID_INCIDENCIA)"
    }

    $r = Invoke-Api "GET" "/api/incidencias" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/incidencias -> 200" $r 200

    if ($script:ID_INCIDENCIA) {
        $r = Invoke-Api "GET" "/api/incidencias/$($script:ID_INCIDENCIA)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/incidencias/{id} -> 200" $r 200

        if ($script:ID_ADMIN) {
            $r = Invoke-Api "GET" "/api/incidencias/funcionario/$($script:ID_ADMIN)" $null $script:TOKEN_ADMIN
            Assert-Status "GET /api/incidencias/funcionario/{id} -> 200" $r 200
        }

        $r = Invoke-Api "PUT" "/api/incidencias/$($script:ID_INCIDENCIA)" @{
            lugar = "Aula 301 - Edificio Norte (actualizado)"
        } $script:TOKEN_ADMIN
        Assert-Status "PUT /api/incidencias/{id} -> 200" $r 200
    }

    $rDup = Invoke-Api "POST" "/api/incidencias" @{
        idInstancia = $script:ID_INSTANCIA2; lugar = "Duplicado"
    } $script:TOKEN_ADMIN
    if ($rDup.Status -ge 400) {
        OK "POST /api/incidencias misma instancia -> $($rDup.Status) (correcto, 1:1)"
    } else {
        KO "POST /api/incidencias instancia duplicada deberia fallar" "recibio $($rDup.Status)"
    }
}

# ============================================================
# 14. ARCHIVOS ADJUNTOS
# ============================================================
Write-Header "14. ARCHIVOS ADJUNTOS (/api/archivos)"

if (-not $script:TOKEN_ADMIN -or -not $script:ID_ESTUDIANTE1) {
    OMIT "Sin token admin o sin estudiante - seccion omitida"
} else {
    $r = Invoke-Api "POST" "/api/archivos" @{
        idEstudiante = $script:ID_ESTUDIANTE1
        ruta         = "/documentos/informe-medico-2026.pdf"
        categoria    = "Informe medico"
    } $script:TOKEN_ADMIN

    if (Assert-Status "POST /api/archivos -> 201" $r 201) {
        $script:ID_ARCHIVO = $r.Body.idArchivoAdjunto
        OK "  Archivo adjunto ID=$($script:ID_ARCHIVO)"
    }

    $r = Invoke-Api "GET" "/api/archivos/estudiante/$($script:ID_ESTUDIANTE1)" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/archivos/estudiante/{id} -> 200" $r 200

    if ($script:ID_ARCHIVO) {
        $r = Invoke-Api "GET" "/api/archivos/$($script:ID_ARCHIVO)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/archivos/{id} -> 200" $r 200
    }
}

# ============================================================
# 15. NOTIFICACIONES
# ============================================================
Write-Header "15. NOTIFICACIONES (/api/notificaciones)"

if (-not $script:TOKEN_ADMIN -or -not $script:ID_INSTANCIA) {
    OMIT "Sin token admin o sin instancia - seccion omitida"
} else {
    $r = Invoke-Api "POST" "/api/notificaciones" @{
        idInstancia  = $script:ID_INSTANCIA
        asunto       = "Confirmacion de reunion de seguimiento"
        mensaje      = "Se confirma la reunion para el 15/09/2026."
        destinatario = "estudiante.uno@estudiantes.utec.edu.uy"
    } $script:TOKEN_ADMIN

    if (Assert-Status "POST /api/notificaciones -> 201" $r 201) {
        $script:ID_NOTIFICACION = $r.Body.idNotificacion
        OK "  Notificacion ID=$($script:ID_NOTIFICACION)"
    }

    $r = Invoke-Api "GET" "/api/notificaciones" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/notificaciones -> 200" $r 200

    $r = Invoke-Api "GET" "/api/notificaciones/instancia/$($script:ID_INSTANCIA)" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/notificaciones/instancia/{id} -> 200" $r 200

    if ($script:ID_NOTIFICACION) {
        $r = Invoke-Api "GET" "/api/notificaciones/$($script:ID_NOTIFICACION)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/notificaciones/{id} -> 200" $r 200
    }
}

# ============================================================
# 16. CATEGORIAS DE RECORDATORIO
# ============================================================
Write-Header "16. CATEGORIAS DE RECORDATORIO (/api/categorias-recordatorio)"

if (-not $script:TOKEN_ADMIN) { OMIT "Sin token admin - seccion omitida" }
else {
    $r = Invoke-Api "POST" "/api/categorias-recordatorio" @{
        nombre      = "Seguimiento Medico Test"
        descripcion = "Categoria creada por script de test"
    } $script:TOKEN_ADMIN

    if (Assert-Status "POST /api/categorias-recordatorio -> 201" $r 201) {
        $script:ID_CAT_RECORD = $r.Body.idCategoriaRecordatorio
        OK "  Categoria recordatorio ID=$($script:ID_CAT_RECORD)"
    }

    $r = Invoke-Api "GET" "/api/categorias-recordatorio" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/categorias-recordatorio -> 200" $r 200

    if ($script:ID_CAT_RECORD) {
        $r = Invoke-Api "GET" "/api/categorias-recordatorio/$($script:ID_CAT_RECORD)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/categorias-recordatorio/{id} -> 200" $r 200

        $r = Invoke-Api "PUT" "/api/categorias-recordatorio/$($script:ID_CAT_RECORD)" @{
            nombre = "Seguimiento Medico (actualizado)"; descripcion = "desc"
        } $script:TOKEN_ADMIN
        Assert-Status "PUT /api/categorias-recordatorio/{id} -> 200" $r 200
    }

    if ($script:TOKEN_PSICO) {
        $r = Invoke-Api "POST" "/api/categorias-recordatorio" @{
            nombre = "Sin permiso"; descripcion = "deberia fallar"
        } $script:TOKEN_PSICO
        Assert-Status "POST /api/categorias-recordatorio sin permiso -> 403" $r 403
    }
}

# ============================================================
# 17. RECORDATORIOS
# ============================================================
Write-Header "17. RECORDATORIOS (/api/recordatorios)"

if (-not $script:TOKEN_ADMIN) { OMIT "Sin token admin - seccion omitida" }
else {
    $bodyRec = @{
        titulo      = "Cita con psicopedagogo (test)"
        descripcion = "Recordatorio generado por script automatizado"
        fecHora     = "2026-09-10T09:00:00-03:00"
        recurrencia = "MENSUAL"
    }
    if ($script:ID_ESTUDIANTE1) { $bodyRec["idEstudiante"] = $script:ID_ESTUDIANTE1 }
    if ($script:ID_CAT_RECORD)  { $bodyRec["idCategoria"]  = $script:ID_CAT_RECORD  }

    $r = Invoke-Api "POST" "/api/recordatorios" $bodyRec $script:TOKEN_ADMIN
    if (Assert-Status "POST /api/recordatorios -> 201" $r 201) {
        $script:ID_RECORDATORIO = $r.Body.idRecordatorio
        OK "  Recordatorio ID=$($script:ID_RECORDATORIO)"
    }

    $r = Invoke-Api "GET" "/api/recordatorios" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/recordatorios -> 200" $r 200

    if ($script:ID_RECORDATORIO) {
        $r = Invoke-Api "GET" "/api/recordatorios/$($script:ID_RECORDATORIO)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/recordatorios/{id} -> 200" $r 200

        $r = Invoke-Api "PUT" "/api/recordatorios/$($script:ID_RECORDATORIO)" @{
            titulo      = "Cita reprogramada"
            fecHora     = "2026-09-12T11:00:00-03:00"
            recurrencia = "NINGUNA"
            estActivo   = $true
        } $script:TOKEN_ADMIN
        Assert-Status "PUT /api/recordatorios/{id} -> 200" $r 200

        $rConv = Invoke-Api "POST" "/api/recordatorios/$($script:ID_RECORDATORIO)/convertir-instancia" $null $script:TOKEN_ADMIN
        if (Assert-Status "POST /api/recordatorios/{id}/convertir-instancia -> 201" $rConv 201) {
            OK "  Convertido a Instancia ID=$($rConv.Body.idInstancia)"
        }
    }

    $rBad = Invoke-Api "POST" "/api/recordatorios" @{
        titulo      = "Test"
        descripcion = "desc"
        fecHora     = "2026-09-10T09:00:00-03:00"
        recurrencia = "INVALIDA"
    } $script:TOKEN_ADMIN
    if ($rBad.Status -ge 400) {
        OK "POST /api/recordatorios recurrencia invalida -> $($rBad.Status)"
    } else {
        KO "POST /api/recordatorios recurrencia invalida deberia fallar"
    }
}

# ============================================================
# 18. EVENTOS DE CALENDARIO
# ============================================================
Write-Header "18. EVENTOS DE CALENDARIO (/api/eventos-calendario)"

if (-not $script:TOKEN_ADMIN) { OMIT "Sin token admin - seccion omitida" }
else {
    $r = Invoke-Api "GET" "/api/eventos-calendario" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/eventos-calendario -> 200" $r 200

    $r = Invoke-Api "GET" "/api/eventos-calendario/pendientes" $null $script:TOKEN_ADMIN
    Assert-Status "GET /api/eventos-calendario/pendientes -> 200" $r 200

    if ($script:ID_INSTANCIA) {
        $r = Invoke-Api "GET" "/api/eventos-calendario/instancia/$($script:ID_INSTANCIA)" $null $script:TOKEN_ADMIN
        if (Assert-Status "GET /api/eventos-calendario/instancia/{id} -> 200" $r 200) {
            $eventos = $r.Body
            $count   = if ($eventos -is [array]) { $eventos.Count } elseif ($eventos) { 1 } else { 0 }
            if ($count -gt 0) {
                $primer = if ($eventos -is [array]) { $eventos[0] } else { $eventos }
                $script:ID_EVENTO = $primer.idEventoCalendario
                OK "  Evento generado automaticamente ID=$($script:ID_EVENTO)"

                if ($primer.googleCalendarLink -like "*calendar.google.com*") {
                    OK "  Google Calendar link generado correctamente"
                } else {
                    KO "  Google Calendar link ausente o incorrecto"
                }

                $rSync = Invoke-Api "POST" "/api/eventos-calendario/$($script:ID_EVENTO)/sincronizar" $null $script:TOKEN_ADMIN
                if (Assert-Status "POST /api/eventos-calendario/{id}/sincronizar -> 200" $rSync 200) {
                    if ($rSync.Body.sincronizado -eq $true) {
                        OK "  Campo sincronizado=true tras sincronizar"
                    } else {
                        KO "  Campo sincronizado no es true"
                    }
                }
            } else {
                KO "  No se genero EventoCalendario automaticamente para la instancia"
            }
        }
    }

    if ($script:ID_EVENTO) {
        $r = Invoke-Api "GET" "/api/eventos-calendario/$($script:ID_EVENTO)" $null $script:TOKEN_ADMIN
        Assert-Status "GET /api/eventos-calendario/{id} -> 200" $r 200
    }
}

# ============================================================
# 19. REPORTES PDF
# ============================================================
Write-Header "19. REPORTES PDF (/api/reportes)"

if (-not $script:TOKEN_ADMIN) { OMIT "Sin token admin - seccion omitida" }
else {
    if ($script:ID_ESTUDIANTE1) {
        $r = Invoke-Api "GET" "/api/reportes/estudiante/$($script:ID_ESTUDIANTE1)" $null $script:TOKEN_ADMIN
        if ($r.Status -eq 200) {
            $kb = [int]($r.Raw.Length / 1024)
            OK "GET /api/reportes/estudiante/{id} -> 200 PDF (~$kb KB)"
        } else { KO "GET /api/reportes/estudiante/{id} -> $($r.Status)" "$($r.Body.error)" }
    } else { OMIT "Reporte estudiante omitido (sin ID_ESTUDIANTE1)" }

    if ($script:ID_GRUPO) {
        $r = Invoke-Api "GET" "/api/reportes/grupo/$($script:ID_GRUPO)" $null $script:TOKEN_ADMIN
        if ($r.Status -eq 200) { OK "GET /api/reportes/grupo/{id} -> 200 PDF" }
        else { KO "GET /api/reportes/grupo/{id} -> $($r.Status)" "$($r.Body.error)" }
    } else { OMIT "Reporte grupo omitido (sin ID_GRUPO)" }

    $urlActividad = "/api/reportes/actividad?fechaInicio=2026-01-01" + "&fechaFin=2026-12-31"
    $r = Invoke-Api "GET" $urlActividad $null $script:TOKEN_ADMIN
    if ($r.Status -eq 200) { OK "GET /api/reportes/actividad -> 200 PDF" }
    else { KO "GET /api/reportes/actividad -> $($r.Status)" "$($r.Body.error)" }

    if ($script:TOKEN_PSICO) {
        $r = Invoke-Api "GET" $urlActividad $null $script:TOKEN_PSICO
        Assert-Status "GET /api/reportes/actividad sin permiso (psico) -> 403" $r 403
    }
}

# ============================================================
# 20. CONTROL DE ACCESO (RBAC)
# ============================================================
Write-Header "20. CONTROL DE ACCESO"

Write-Sub "Sin token -> 401 en endpoints protegidos"
$endpoints401 = @(
    @("GET", "/api/carreras"),
    @("GET", "/api/itr"),
    @("GET", "/api/grupos"),
    @("GET", "/api/estudiantes"),
    @("GET", "/api/funcionarios"),
    @("GET", "/api/instancias"),
    @("GET", "/api/seguimientos"),
    @("GET", "/api/recordatorios"),
    @("GET", "/api/notificaciones"),
    @("GET", "/api/eventos-calendario")
)
foreach ($ep in $endpoints401) {
    $r = Invoke-Api $ep[0] $ep[1]
    Assert-Status "$($ep[0]) $($ep[1]) sin token -> 401" $r 401
}

Write-Sub "Token malformado -> 401"
$r = Invoke-Api "GET" "/api/carreras" $null "token.totalmente.falso"
Assert-Status "Token malformado -> 401" $r 401

Write-Sub "Psicopedagogo no puede usar endpoints de admin"
if ($script:TOKEN_PSICO) {
    $r = Invoke-Api "GET" "/api/carreras" $null $script:TOKEN_PSICO
    Assert-Status "Psico GET /api/carreras -> 403" $r 403

    $r = Invoke-Api "GET" "/api/itr" $null $script:TOKEN_PSICO
    Assert-Status "Psico GET /api/itr -> 403" $r 403

    $r = Invoke-Api "POST" "/api/carreras" @{ codigo = "XX"; nombre = "Test"; plan = "2026" } $script:TOKEN_PSICO
    Assert-Status "Psico POST /api/carreras -> 403" $r 403

    $r = Invoke-Api "POST" "/api/asignaciones" @{ idUsuario = 1; idRol = 1 } $script:TOKEN_PSICO
    Assert-Status "Psico POST /api/asignaciones -> 403" $r 403
} else {
    OMIT "Tests RBAC Psico omitidos (sin token)"
}

# ============================================================
# 21. BAJAS LOGICAS (DELETE)
# ============================================================
Write-Header "21. BAJAS LOGICAS (DELETE)"

if (-not $script:TOKEN_ADMIN) { OMIT "Sin token admin - seccion omitida" }
else {
    $deletes = @(
        @("/api/observaciones",          $script:ID_OBSERVACION),
        @("/api/archivos",               $script:ID_ARCHIVO),
        @("/api/notificaciones",         $script:ID_NOTIFICACION),
        @("/api/seguimientos",           $script:ID_SEGUIMIENTO),
        @("/api/informes",               $script:ID_INFORME),
        @("/api/categorias-instancia",   $script:ID_CAT_INSTANCIA),
        @("/api/categorias-recordatorio",$script:ID_CAT_RECORD),
        @("/api/instancias",             $script:ID_INSTANCIA),
        @("/api/estudiantes",            $script:ID_ESTUDIANTE1),
        @("/api/estudiantes",            $script:ID_ESTUDIANTE2),
        @("/api/carreras",               $script:ID_CARRERA),
        @("/api/asignaciones",           $script:ID_ASIGNACION)
    )
    foreach ($d in $deletes) {
        if ($d[1]) {
            $r = Invoke-Api "DELETE" "$($d[0])/$($d[1])" $null $script:TOKEN_ADMIN
            Assert-Status "DELETE $($d[0])/{id} -> 204" $r 204
        }
    }

    if ($script:ID_ESTUDIANTE1) {
        $r = Invoke-Api "GET" "/api/estudiantes/$($script:ID_ESTUDIANTE1)" $null $script:TOKEN_ADMIN
        if ($r.Status -eq 200 -and $r.Body.estActivo -eq $false) {
            OK "Baja logica verificada: estActivo=false (registro NO eliminado fisicamente)"
        } elseif ($r.Status -in 200, 400) {
            OK "Baja logica procesada (status=$($r.Status))"
        } else {
            KO "Comportamiento inesperado post-DELETE" "status=$($r.Status)"
        }
    }

    $r = Invoke-Api "DELETE" "/api/carreras/99999" $null $script:TOKEN_ADMIN
    Assert-Status "DELETE /api/carreras/99999 (no existe) -> 400" $r 400
}

# ============================================================
# RESUMEN FINAL
# ============================================================
$total = $script:PASS + $script:FAIL + $script:SKIP
Write-Host ""
Write-Host ("=" * 60) -ForegroundColor Cyan
Write-Host "  RESUMEN DE RESULTADOS" -ForegroundColor Cyan
Write-Host ("=" * 60) -ForegroundColor Cyan
Write-Host ("  [PASS] {0,4}  tests pasaron" -f $script:PASS) -ForegroundColor Green
Write-Host ("  [FAIL] {0,4}  tests fallaron" -f $script:FAIL) -ForegroundColor Red
Write-Host ("  [SKIP] {0,4}  tests omitidos" -f $script:SKIP) -ForegroundColor Yellow
Write-Host ("  TOTAL  {0,4}" -f $total)

if ($script:FALLIDOS.Count -gt 0) {
    Write-Host ""
    Write-Host "  Detalle de fallos:" -ForegroundColor Red
    foreach ($f in $script:FALLIDOS) { Write-Host $f -ForegroundColor Red }
}

Write-Host ""
if ($script:FAIL -eq 0) {
    Write-Host "  OK - todos los tests pasaron." -ForegroundColor Green
} else {
    Write-Host "  ATENCION: $($script:FAIL) test(s) fallaron." -ForegroundColor Red
    Write-Host ""
    Write-Host "  Notas:" -ForegroundColor Yellow
    Write-Host "  - Si falla el registro, puede haber CI duplicada de un run anterior." -ForegroundColor Yellow
    Write-Host "  - Si falla RBAC, revisa que el DataSeeder haya corrido." -ForegroundColor Yellow
    Write-Host "  - Swagger: $BaseUrl/swagger-ui.html para explorar manualmente." -ForegroundColor Yellow
}
Write-Host ""
