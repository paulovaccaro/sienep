# SIENEP — Backend API

**Sistema de Seguimiento Integral de Estudiantes con Necesidades Educativas Personalizadas**

API RESTful desarrollada con Spring Boot para UTEC (Universidad Tecnológica del Uruguay). Gestiona estudiantes, instancias educativas, seguimientos, recordatorios, incidencias, auditoría y reportes, con seguridad basada en JWT y control de acceso por roles (RBAC).

---

## Tabla de contenidos

1. [Stack tecnológico](#stack-tecnológico)
2. [Requisitos previos](#requisitos-previos)
3. [Configuración y ejecución](#configuración-y-ejecución)
4. [Autenticación](#autenticación)
5. [Sistema de roles y permisos (RBAC)](#sistema-de-roles-y-permisos-rbac)
6. [Validaciones de negocio](#validaciones-de-negocio)
7. [Referencia de la API](#referencia-de-la-api)
   - [Auth](#auth---authcontroller)
   - [Carreras](#carreras---carreracontroller)
   - [ITR](#itr---itrcontroller)
   - [Grupos](#grupos---grupocontroller)
   - [Estudiantes](#estudiantes---estudiantecontroller)
   - [Funcionarios](#funcionarios---funcionariocontroller)
   - [Asignaciones](#asignaciones-rbac---asignacioncontroller)
   - [Instancias](#instancias---instanciacontroller)
   - [Categorías de Instancia](#categorías-de-instancia---categoriainstanciacontroller)
   - [Seguimientos](#seguimientos---seguimientocontroller)
   - [Observaciones](#observaciones---observacioncontroller)
   - [Incidencias](#incidencias---incidenciacontroller)
   - [Informes Finales](#informes-finales---informefinalcontroller)
   - [Archivos Adjuntos](#archivos-adjuntos---archivoadjuntocontroller)
   - [Recordatorios](#recordatorios---recordatoriocontroller)
   - [Categorías de Recordatorio](#categorías-de-recordatorio---categoriarecordatoriocontroller)
   - [Notificaciones](#notificaciones---notificacioncontroller)
   - [Eventos de Calendario](#eventos-de-calendario-google-calendar-simulado---eventocalendariocontroller)
   - [Reportes](#reportes---reportecontroller)
8. [Documentación interactiva (Swagger)](#documentación-interactiva-swagger)
9. [Base de datos](#base-de-datos)
10. [Auditoría y trazabilidad](#auditoría-y-trazabilidad)

---

## Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje |
| Spring Boot | 4.0.6 | Framework principal |
| Spring Security | 4.0.6 | Autenticación y autorización |
| JJWT | 0.12.6 | Generación y validación de tokens JWT |
| Spring Data JPA / Hibernate | — | Persistencia con ORM |
| PostgreSQL (Supabase) | — | Base de datos |
| Springdoc OpenAPI | — | Documentación Swagger |
| OpenPDF | 1.3.30 | Generación de reportes PDF |
| Lombok | — | Reducción de boilerplate |
| BCrypt | — | Hashing de contraseñas |

---

## Requisitos previos

- Java 21+
- Maven 3.9+ (o usar el wrapper `./mvnw`)
- Acceso a la base de datos PostgreSQL (Supabase)

---

## Configuración y ejecución

### Variables de entorno

La aplicación lee la configuración sensible desde un archivo `.env` en la raíz del proyecto (al lado de `pom.xml`). Este archivo **nunca se commitea** — está en `.gitignore`.

**Paso 1:** copiar el ejemplo y completar los valores reales:

```bash
cp .env.example .env
# Editar .env con los valores reales
```

| Variable | Descripción | Requerida |
|---|---|---|
| `DB_PASSWORD` | Contraseña de la base de datos PostgreSQL (Supabase) | Sí |
| `JWT_SECRET` | Clave secreta para firmar JWT (mínimo 256 bits / 32 caracteres) | No — tiene valor de desarrollo por defecto |

> **Importante:** el `.env` va en la raíz del proyecto (al lado de `pom.xml`), no dentro de `src/`. Se lee en runtime mediante `spring.config.import` y **no se empaqueta en el jar**.

### Ejecutar localmente

```bash
# Clonar el repositorio y posicionarse en la carpeta
cd sienep

# Crear y completar el .env (ver sección anterior)
cp .env.example .env

# Compilar
./mvnw clean compile

# Ejecutar
./mvnw spring-boot:run
```

La aplicación inicia en `http://localhost:8080`.

### Configuración de base de datos

La URL de conexión, usuario y contraseña están en `src/main/resources/application.properties`. La contraseña se inyecta desde el `.env` vía `${DB_PASSWORD}` — **no editar `application.properties` para poner credenciales directamente**.

> **Importante:** `ddl-auto=validate` exige que el esquema en la base de datos coincida exactamente con las entidades JPA. Ejecutar el script SQL antes de iniciar la aplicación.

---

## Autenticación

La API usa **JWT Bearer Token**. Todos los endpoints bajo `/api/**` requieren el header:

```
Authorization: Bearer <token>
```

### Flujo de autenticación

```
1. POST /auth/registro  →  obtener token JWT
        ó
   POST /auth/login     →  obtener token JWT

2. Incluir el token en cada request:
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Estructura del token JWT

El token contiene:
- `sub`: username del usuario
- `userId`: ID del usuario (usado internamente para verificar permisos)
- Expiración: 24 horas

---

## Sistema de roles y permisos (RBAC)

El sistema implementa **Role-Based Access Control con scope jerárquico**.

### Roles predefinidos del sistema

| Rol | Descripción |
|---|---|
| Administrador | Acceso total al sistema |
| Psicopedagogo | CRUD operativo de seguimientos, instancias y observaciones |
| Analista Educativo | Lectura operativa en su scope |
| Responsable Educativo | Lectura operativa en su scope |
| Area Legal | Solo lectura + reportes |
| Estudiante | Acceso a información propia |

### Modelo de scope

Una **asignación** vincula a un usuario con un rol y un scope opcional:

| Scope | Descripción |
|---|---|
| Global (todos null) | El usuario tiene el permiso en todo el sistema |
| `id_itr` | El permiso aplica a todos los grupos de ese ITR |
| `id_carrera` | El permiso aplica a todos los grupos de esa carrera |
| `id_grupo` | El permiso aplica únicamente a ese grupo |

### Permisos disponibles

```
usuarios.*         → leer, crear, modificar, eliminar
funcionarios.*     → leer, crear, modificar, eliminar
estudiantes.*      → leer, crear, modificar, eliminar
seguimientos.*     → leer, crear, modificar, eliminar
instancias.*       → leer, crear, modificar, eliminar, gestionar
observaciones.*    → leer, crear, modificar, eliminar
info_final.*       → leer, crear, modificar, eliminar
arch_adjuntos.*    → leer, crear, modificar, eliminar
notificaciones.*   → leer, crear, modificar, eliminar
incidencias.*      → leer, crear, modificar, eliminar
recordatorios.*    → leer, crear, modificar, eliminar, gestionar
roles.gestionar
asignaciones.gestionar
grupos.gestionar
carreras.gestionar
itr.gestionar
reportes.generar, reportes.exportar
```

---

## Validaciones de negocio

### Cédula de identidad uruguaya
Todos los registros de personas validan la cédula mediante el **algoritmo del dígito verificador**. Las cédulas inválidas reciben `400 Bad Request`.

### Edad mínima
Todos los registros de personas verifican que la fecha de nacimiento corresponda a una persona de **al menos 18 años**.

### Bajas lógicas
Ninguna entidad se elimina físicamente. Todas tienen el campo `est_activo` que se pone en `false` al hacer DELETE. Esto preserva el historial completo.

---

## Referencia de la API

### Auth — `AuthController`

**Base:** `/auth` | **Acceso:** Público

#### `POST /auth/login`
Autentica un usuario y devuelve un JWT.

```json
// Request
{
  "username": "juan.perez",
  "password": "contraseña123"
}

// Response 200
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "username": "juan.perez"
}
```

#### `POST /auth/registro`
Registra un nuevo funcionario. Devuelve JWT listo para usar.

```json
// Request
{
  "cedula": "12345678",
  "nombre": "Juan",
  "apellido": "Pérez",
  "password": "contraseña123",
  "fecNacimiento": "1990-05-15",
  "idRol": 1,
  "idItr": 1,      // opcional — scope inicial
  "idCarrera": 1,  // opcional
  "idGrupo": 1     // opcional
}

// Response 201
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 5,
  "username": "juan.perez"
}
```

#### `POST /auth/aceptar-politicas`
Activa un usuario inactivo (que aún no aceptó políticas) y devuelve JWT.

```json
// Request — mismo formato que /auth/login
{ "username": "juan.perez", "password": "contraseña123" }
```

---

### Carreras — `CarreraController`

**Base:** `/api/carreras` | **Permiso:** `carreras.gestionar` (global)

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/carreras` | Listar carreras |
| GET | `/api/carreras/{id}` | Obtener carrera |
| POST | `/api/carreras` | Crear carrera |
| PUT | `/api/carreras/{id}` | Modificar carrera |
| DELETE | `/api/carreras/{id}` | Baja lógica |

```json
// POST /api/carreras — Request
{
  "codigo": "LTI",
  "nombre": "Licenciatura en Tecnologías de la Información",
  "plan": "2019"
}

// Response 201
{
  "idCarrera": 1,
  "codigo": "LTI",
  "nombre": "Licenciatura en Tecnologías de la Información",
  "plan": "2019",
  "estActivo": true
}
```

---

### ITR — `ITRController`

**Base:** `/api/itr` | **Permiso:** `itr.gestionar` (global)

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/itr` | Listar ITRs |
| GET | `/api/itr/{id}` | Obtener ITR |
| POST | `/api/itr` | Crear ITR |
| PUT | `/api/itr/{id}` | Modificar ITR |
| DELETE | `/api/itr/{id}` | Baja lógica |

```json
// POST /api/itr — Request
{
  "codigo": "ITR-SUR",
  "nombre": "ITR Sur",
  "idDireccion": 1
}
```

---

### Grupos — `GrupoController`

**Base:** `/api/grupos` | **Permiso:** `grupos.gestionar` (global)

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/grupos?idItr=1` | Listar grupos (filtro ITR opcional) |
| GET | `/api/grupos/{id}` | Obtener grupo |
| POST | `/api/grupos` | Crear grupo |
| PUT | `/api/grupos/{id}` | Modificar grupo |
| DELETE | `/api/grupos/{id}` | Baja lógica |

```json
// POST /api/grupos — Request
{
  "nomGrupo": "LTI-SUR-2025-1",
  "idCarrera": 1,
  "idItr": 1,
  "anio": 2025,
  "semestre": 1
}
```

---

### Estudiantes — `EstudianteController`

**Base:** `/api/estudiantes` | **Permisos:** `estudiantes.*` (scope por grupo)

| Método | URL | Permiso | Descripción |
|---|---|---|---|
| GET | `/api/estudiantes` | `estudiantes.leer` | Listar estudiantes accesibles |
| GET | `/api/estudiantes/{id}` | `estudiantes.leer` | Obtener (propietario o con permiso) |
| POST | `/api/estudiantes` | `estudiantes.crear` | Alta de estudiante |
| PUT | `/api/estudiantes/{id}` | `estudiantes.modificar` | Modificar (propietario o con permiso) |
| DELETE | `/api/estudiantes/{id}` | `estudiantes.eliminar` | Baja lógica |

```json
// POST /api/estudiantes — Request
{
  "cedula": "87654321",
  "nombre": "María",
  "apellido": "González",
  "password": "pass1234",
  "fecNacimiento": "2000-03-10",
  "idGrupo": 1
}

// Response 201
{
  "idUsuario": 10,
  "cedula": "87654321",
  "nombre": "María",
  "apellido": "González",
  "username": "maria.gonzalez",
  "correo": "maria.gonzalez@estudiantes.utec.edu.uy",
  "fecNacimiento": "2000-03-10",
  "idGrupo": 1,
  "nomGrupo": "LTI-SUR-2025-1",
  "estActivo": true
}

// PUT /api/estudiantes/{id} — Request
{
  "nombre": "María José",
  "apellido": "González",
  "idGrupo": 2,
  "estActivo": true
}
```

---

### Funcionarios — `FuncionarioController`

**Base:** `/api/funcionarios` | **Permisos:** `funcionarios.*` (global)

| Método | URL | Permiso | Descripción |
|---|---|---|---|
| GET | `/api/funcionarios` | `funcionarios.leer` | Listar funcionarios |
| GET | `/api/funcionarios/{id}` | `funcionarios.leer` | Obtener (propietario o con permiso) |
| POST | `/api/funcionarios` | `funcionarios.crear` | Crear funcionario |
| DELETE | `/api/funcionarios/{id}` | `funcionarios.eliminar` | Baja lógica |

```json
// POST /api/funcionarios — Request
{
  "cedula": "11223344",
  "nombre": "Carlos",
  "apellido": "Rodríguez",
  "password": "pass1234",
  "fecNacimiento": "1985-07-20"
}
```

---

### Asignaciones (RBAC) — `AsignacionController`

**Base:** `/api/asignaciones` | **Permiso:** `asignaciones.gestionar` (global)

Gestiona la relación Usuario → Rol → Scope. Permite definir qué puede hacer cada usuario y sobre qué recursos.

| Método | URL | Descripción |
|---|---|---|
| POST | `/api/asignaciones` | Asignar rol a usuario |
| GET | `/api/asignaciones/{id}` | Obtener asignación |
| GET | `/api/asignaciones/usuario/{idUsuario}` | Asignaciones de un usuario |
| PUT | `/api/asignaciones/{id}` | Modificar asignación |
| DELETE | `/api/asignaciones/{id}` | Baja lógica |

```json
// POST /api/asignaciones — Request (scope global)
{
  "idUsuario": 5,
  "idRol": 2
}

// POST /api/asignaciones — Request (scope por grupo)
{
  "idUsuario": 5,
  "idRol": 3,
  "idGrupo": 1
}

// PUT /api/asignaciones/{id} — Request
{
  "idRol": 2,
  "idGrupo": 2,
  "limpiarItr": true,
  "limpiarCarrera": true,
  "estActivo": true
}
// Nota: limpiarItr/limpiarCarrera/limpiarGrupo = true remueve ese scope
```

---

### Instancias — `InstanciaController`

**Base:** `/api/instancias` | **Permisos:** `instancias.*` (global)

Las instancias representan eventos o reuniones educativas. Al crear una instancia se genera automáticamente un **EventoCalendario** (simulación Google Calendar).

| Método | URL | Permiso | Descripción |
|---|---|---|---|
| GET | `/api/instancias` | `instancias.leer` | Listar instancias |
| GET | `/api/instancias/{id}` | `instancias.leer` | Obtener instancia |
| POST | `/api/instancias` | `instancias.crear` | Crear instancia |
| PUT | `/api/instancias/{id}` | `instancias.modificar` | Modificar instancia |
| POST | `/api/instancias/{id}/clonar` | `instancias.crear` | Clonar instancia (RF17) |
| DELETE | `/api/instancias/{id}` | `instancias.eliminar` | Baja lógica |

```json
// POST /api/instancias — Request
{
  "titulo": "Reunión de seguimiento",
  "tipo": "Reunión",
  "fecHora": "2026-06-15T10:00:00-03:00",
  "descripcion": "Seguimiento semestral del estudiante",
  "idFuncionario": 5,
  "idCategoria": 1  // opcional — id de CategoriaInstancia
}

// Response 201
{
  "idInstancia": 1,
  "titulo": "Reunión de seguimiento",
  "tipo": "Reunión",
  "fecHora": "2026-06-15T10:00:00-03:00",
  "descripcion": "Seguimiento semestral del estudiante",
  "estActivo": true,
  "idFuncionario": 5,
  "idCategoriaInstancia": 1,
  "nombreCategoria": "Seguimiento académico"
}

// POST /api/instancias/{id}/clonar — Request (body opcional)
{
  "fecHora": "2026-07-01T10:00:00-03:00"
}
```

---

### Categorías de Instancia — `CategoriaInstanciaController`

**Base:** `/api/categorias-instancia` | **Permiso CUD:** `instancias.gestionar`

Catálogo administrable de categorías para clasificar instancias (RF35–RF37).

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/categorias-instancia` | Listar categorías |
| GET | `/api/categorias-instancia/{id}` | Obtener categoría |
| POST | `/api/categorias-instancia` | Crear categoría (RF35) |
| PUT | `/api/categorias-instancia/{id}` | Modificar categoría (RF37) |
| DELETE | `/api/categorias-instancia/{id}` | Baja lógica (RF36) |

```json
// POST /api/categorias-instancia — Request
{
  "nombre": "Seguimiento académico",
  "descripcion": "Instancias relacionadas con el seguimiento educativo del estudiante"
}
```

---

### Seguimientos — `SeguimientoController`

**Base:** `/api/seguimientos` | **Permisos:** `seguimientos.*` (scope por grupo)

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/seguimientos` | Seguimientos accesibles |
| GET | `/api/seguimientos/{id}` | Obtener seguimiento |
| GET | `/api/seguimientos/estudiante/{idEstudiante}` | Seguimientos de un estudiante |
| POST | `/api/seguimientos` | Crear seguimiento |
| PUT | `/api/seguimientos/{id}` | Modificar seguimiento |
| DELETE | `/api/seguimientos/{id}` | Baja lógica |

```json
// POST /api/seguimientos — Request
{
  "idEstudiante": 10,
  "fecInicio": "2026-03-01",
  "fecCierre": "2026-07-01",
  "idInforme": null
}
```

---

### Observaciones — `ObservacionController`

**Base:** `/api/observaciones` | **Permisos:** `observaciones.*` (scope por grupo)

El funcionario autenticado queda registrado como autor de la observación.

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/observaciones/estudiante/{idEstudiante}` | Observaciones de un estudiante |
| GET | `/api/observaciones/{id}` | Obtener observación |
| POST | `/api/observaciones` | Crear observación |
| DELETE | `/api/observaciones/{id}` | Baja lógica |

```json
// POST /api/observaciones — Request
{
  "idEstudiante": 10,
  "titulo": "Dificultades en comprensión lectora",
  "contenido": "El estudiante presenta dificultades persistentes..."
}
```

---

### Incidencias — `IncidenciaController`

**Base:** `/api/incidencias` | **Permisos:** `incidencias.*` (global)

Cada incidencia está asociada a una instancia preexistente. Al eliminar una incidencia también se desactiva la instancia subyacente.

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/incidencias` | Listar incidencias |
| GET | `/api/incidencias/{id}` | Obtener incidencia |
| GET | `/api/incidencias/funcionario/{idFuncionario}` | Por funcionario |
| POST | `/api/incidencias` | Registrar incidencia |
| PUT | `/api/incidencias/{id}` | Modificar lugar |
| DELETE | `/api/incidencias/{id}` | Baja lógica (desactiva instancia también) |

```json
// POST /api/incidencias — Request
{
  "idInstancia": 3,
  "lugar": "Aula 204 — Edificio Central"
}
```

---

### Informes Finales — `InformeFinalController`

**Base:** `/api/informes` | **Permisos:** `info_final.*` (global)

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/informes` | Listar informes |
| GET | `/api/informes/{id}` | Obtener informe |
| POST | `/api/informes` | Crear informe |
| PUT | `/api/informes/{id}` | Modificar informe |
| DELETE | `/api/informes/{id}` | Baja lógica |

```json
// POST /api/informes — Request
{
  "contenido": "El estudiante ha mejorado su rendimiento...",
  "valoracion": 8,
  "fecCreacion": "2026-06-30"
}
```

---

### Archivos Adjuntos — `ArchivoAdjuntoController`

**Base:** `/api/archivos` | **Permisos:** `arch_adjuntos.*` (scope por grupo)

Registro de archivos adjuntos (informes médicos, documentación, etc.) asociados a estudiantes.

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/archivos/estudiante/{idEstudiante}` | Archivos de un estudiante |
| GET | `/api/archivos/{id}` | Obtener archivo |
| POST | `/api/archivos` | Registrar archivo |
| DELETE | `/api/archivos/{id}` | Baja lógica |

```json
// POST /api/archivos — Request
{
  "idEstudiante": 10,
  "ruta": "/documentos/informes/medico-2026.pdf",
  "categoria": "Informe médico"
}
```

---

### Recordatorios — `RecordatorioController`

**Base:** `/api/recordatorios` | **Permisos:** `recordatorios.*` (global)

Al crear un recordatorio se genera automáticamente un **EventoCalendario**. Los propietarios siempre pueden gestionar sus propios recordatorios sin necesitar el permiso global.

| Método | URL | Permiso | Descripción |
|---|---|---|---|
| GET | `/api/recordatorios` | `recordatorios.leer` | Todos (global) o propios |
| GET | `/api/recordatorios/{id}` | — | Propietario o con permiso |
| POST | `/api/recordatorios` | `recordatorios.crear` | Crear recordatorio |
| PUT | `/api/recordatorios/{id}` | — | Propietario o con permiso |
| DELETE | `/api/recordatorios/{id}` | — | Propietario o con permiso |
| POST | `/api/recordatorios/{id}/convertir-instancia` | — | Convertir en instancia (RF27) |

```json
// POST /api/recordatorios — Request
{
  "titulo": "Cita con psicopedagogo",
  "descripcion": "Reunión mensual de seguimiento",
  "fecHora": "2026-06-10T09:00:00-03:00",
  "recurrencia": "MENSUAL",
  "idEstudiante": 10,
  "idCategoria": 2
}
// Nota: idFuncionario se toma automáticamente del JWT

// PUT /api/recordatorios/{id} — Request
{
  "titulo": "Cita con psicopedagogo (reprogramada)",
  "fecHora": "2026-06-12T11:00:00-03:00",
  "recurrencia": "NINGUNA",
  "estActivo": true
}

// POST /api/recordatorios/{id}/convertir-instancia
// Sin body — convierte el recordatorio en una Instancia y lo desactiva
// Response 201: InstanciaResponseDto
```

**Valores de recurrencia:** `NINGUNA` | `DIARIA` | `SEMANAL` | `MENSUAL` | `ANUAL`

---

### Categorías de Recordatorio — `CategoriaRecordatorioController`

**Base:** `/api/categorias-recordatorio` | **Permiso CUD:** `recordatorios.gestionar`

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/categorias-recordatorio` | Listar categorías |
| GET | `/api/categorias-recordatorio/{id}` | Obtener categoría |
| POST | `/api/categorias-recordatorio` | Crear categoría |
| PUT | `/api/categorias-recordatorio/{id}` | Modificar categoría |
| DELETE | `/api/categorias-recordatorio/{id}` | Baja lógica |

```json
// POST /api/categorias-recordatorio — Request
{
  "nombre": "Seguimiento médico",
  "descripcion": "Recordatorios relacionados con citas y controles médicos"
}
```

---

### Notificaciones — `NotificacionController`

**Base:** `/api/notificaciones` | **Permisos:** `notificaciones.*` (global)

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/notificaciones` | Listar notificaciones |
| GET | `/api/notificaciones/{id}` | Obtener notificación |
| GET | `/api/notificaciones/instancia/{idInstancia}` | Por instancia |
| POST | `/api/notificaciones` | Crear notificación |
| DELETE | `/api/notificaciones/{id}` | Baja lógica |

```json
// POST /api/notificaciones — Request
{
  "idInstancia": 1,
  "asunto": "Confirmación de instancia",
  "mensaje": "Se confirma la reunión de seguimiento para el 15/06.",
  "destinatario": "maria.gonzalez@estudiantes.utec.edu.uy"
}
```

---

### Eventos de Calendario (Google Calendar simulado) — `EventoCalendarioController`

**Base:** `/api/eventos-calendario` | **Permisos:** `instancias.*` (global)

Implementación de RF13 y RF22. Los eventos se generan automáticamente al crear una Instancia o un Recordatorio. El campo `googleCalendarLink` contiene un link real al formato de Google Calendar (`calendar.google.com/calendar/r/eventedit?...`).

| Método | URL | Permiso | Descripción |
|---|---|---|---|
| GET | `/api/eventos-calendario` | `instancias.leer` | Listar todos los eventos |
| GET | `/api/eventos-calendario/pendientes` | `instancias.leer` | Eventos sin sincronizar |
| GET | `/api/eventos-calendario/{id}` | `instancias.leer` | Obtener evento |
| GET | `/api/eventos-calendario/instancia/{id}` | `instancias.leer` | Eventos de una instancia |
| GET | `/api/eventos-calendario/recordatorio/{id}` | `instancias.leer` | Eventos de un recordatorio |
| POST | `/api/eventos-calendario` | `instancias.crear` | Crear evento manual |
| PUT | `/api/eventos-calendario/{id}` | `instancias.modificar` | Modificar evento |
| POST | `/api/eventos-calendario/{id}/sincronizar` | `instancias.modificar` | **Sincronizar con Google (RF13/RF22)** |
| DELETE | `/api/eventos-calendario/{id}` | `instancias.eliminar` | Baja lógica |

```json
// Response de EventoCalendario
{
  "idEventoCalendario": 1,
  "titulo": "Reunión de seguimiento",
  "descripcion": "Seguimiento semestral del estudiante",
  "fecInicio": "2026-06-15T10:00:00-03:00",
  "fecFin": "2026-06-15T11:00:00-03:00",
  "ubicacion": null,
  "idInstancia": 1,
  "idRecordatorio": null,
  "googleEventId": "SIENEP-INST-A3F7B2C1",
  "googleCalendarLink": "https://calendar.google.com/calendar/r/eventedit?text=Reuni%C3%B3n+de+seguimiento&dates=20260615T130000Z/20260615T140000Z&details=...",
  "sincronizado": false,
  "estActivo": true
}

// POST /api/eventos-calendario/{id}/sincronizar
// Sin body — marca el evento como sincronizado y registra la llamada simulada
// Response 200: EventoCalendarioResponseDto con sincronizado=true
```

---

### Reportes — `ReporteController`

**Base:** `/api/reportes` | **Permiso:** `reportes.generar` (global)

Genera reportes en formato PDF. La respuesta es un archivo descargable.

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/reportes/estudiante/{idEstudiante}` | Reporte individual del estudiante |
| GET | `/api/reportes/grupo/{idGrupo}` | Reporte del grupo |
| GET | `/api/reportes/actividad?fechaInicio=&fechaFin=` | Reporte de actividad por período |

```
// Response: application/pdf
// Content-Disposition: attachment; filename="reporte-estudiante-10.pdf"

// Contenido del reporte de estudiante:
//   - Datos personales y académicos
//   - Seguimientos activos e históricos
//   - Observaciones registradas
//   - Informes finales

// Contenido del reporte de grupo:
//   - Información del grupo (carrera, ITR, año, semestre)
//   - Tabla de estudiantes con conteos de seguimientos e instancias

// Reporte de actividad:
//   GET /api/reportes/actividad?fechaInicio=2026-06-01&fechaFin=2026-06-30
//   - Instancias y recordatorios en el período especificado
```

---

## Documentación interactiva (Swagger)

Con la aplicación corriendo, acceder a:

```
http://localhost:8080/swagger-ui.html
```

Permite explorar y probar todos los endpoints directamente desde el navegador. Para endpoints protegidos:
1. Ejecutar `POST /auth/login`
2. Copiar el token de la respuesta
3. Hacer clic en **Authorize** (ícono del candado)
4. Ingresar `Bearer <token>`

---

## Base de datos

### Motor
PostgreSQL (hosteado en Supabase)

### Script SQL

El esquema completo vive en:

```
src/main/resources/schema.sql
```

Crea todas las tablas desde cero en el orden correcto (respetando FKs). Es el único script necesario para una instalación limpia.

**Ejecutar antes de iniciar la aplicación:**

```bash
psql -U <usuario> -d <base_de_datos> -f src/main/resources/schema.sql
```

> El archivo `src/main/resources/migrations/recordatorios.sql` es una migración incremental histórica. Su contenido ya está incluido en `schema.sql`; no es necesario ejecutarlo en instalaciones nuevas.

> **No** agregues `spring.sql.init.mode` ni `spring.datasource.schema` al `application.properties`. Con `ddl-auto=validate`, Hibernate valida el esquema **antes** de que Spring ejecute la inicialización SQL, lo que rompería el arranque. El script se corre **manualmente** con `psql`.

### Tablas principales

| Tabla | Descripción |
|---|---|
| `usuarios` | Datos base de todos los usuarios |
| `estudiantes` | Especialización de usuario (estudiante) |
| `funcionarios` | Especialización de usuario (funcionario) |
| `grupos` | Grupos académicos por carrera, ITR, año y semestre |
| `carreras` | Carreras disponibles |
| `itr` | Institutos Tecnológicos Regionales |
| `roles` | Roles del sistema (predefinidos + personalizados) |
| `permisos` | Acciones atómicas (`entidad.accion`) |
| `rol_permiso` | Relación M:N rol ↔ permiso |
| `asignaciones` | Relación usuario ↔ rol ↔ scope |
| `instancias` | Eventos/reuniones educativas |
| `categorias_instancia` | Catálogo de categorías de instancias |
| `seguimientos` | Seguimientos educativos de estudiantes |
| `observaciones` | Observaciones de funcionarios |
| `incidencias` | Incidencias (1:1 con instancias) |
| `recordatorios` | Recordatorios de funcionarios |
| `categorias_recordatorio` | Catálogo de categorías de recordatorios |
| `notificaciones` | Notificaciones vinculadas a instancias |
| `arch_adjuntos` | Archivos adjuntos de estudiantes |
| `info_final` | Informes finales de seguimiento |
| `eventos_calendario` | Eventos de calendario (simulación Google Calendar) |
| `auditoria` | Registro de auditoría de todas las operaciones |

---

## Auditoría y trazabilidad

Todas las operaciones relevantes (alta, baja, modificación, login) quedan registradas en la tabla `auditoria` con:

| Campo | Descripción |
|---|---|
| `id_usuario` | Quién ejecutó la acción |
| `accion` | Tipo de operación (`alta`, `baja`, `modificacion`, `login`) |
| `entidad` | Nombre de la tabla afectada |
| `id_entidad` | ID del registro afectado |
| `fec_hora` | Timestamp con zona horaria |
| `detalle` | Payload JSON con datos adicionales del cambio |
