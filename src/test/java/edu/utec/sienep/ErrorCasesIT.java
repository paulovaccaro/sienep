package edu.utec.sienep;

import edu.utec.sienep.dto.IncidenciaCreateDto;
import edu.utec.sienep.dto.InstanciaCreateDto;
import edu.utec.sienep.dto.InstanciaResponseDto;
import edu.utec.sienep.dto.LoginResponseDto;
import edu.utec.sienep.dto.RecordatorioCreateDto;
import edu.utec.sienep.dto.RecordatorioResponseDto;
import edu.utec.sienep.dto.RegistroRequestDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCasesIT extends BaseIT {

    private static final String CI_ADMIN = "30000048";
    private static final String CI_DUPLICADO = "40000046";

    private String adminToken;
    private Integer adminId;

    @BeforeAll
    void setup() {
        RegistroRequestDto req = new RegistroRequestDto();
        req.setCedula(CI_ADMIN);
        req.setNombre("Admin");
        req.setApellido("Err");
        req.setPassword("admin1234");
        req.setFecNacimiento(LocalDate.of(1985, 1, 1));
        req.setIdRol(1);
        ResponseEntity<LoginResponseDto> resp = rest.postForEntity(url("/auth/registro"), req, LoginResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        adminToken = resp.getBody().getToken();
        adminId    = resp.getBody().getUserId();
    }

    // ── Recursos no encontrados → 400 ─────────────────────────────────────────

    @Test
    void instanciaInexistente_devuelve400() {
        ResponseEntity<String> resp = rest.exchange(
                url("/api/instancias/99999"), HttpMethod.GET,
                withAuth(adminToken), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void funcionarioInexistente_devuelve400() {
        ResponseEntity<String> resp = rest.exchange(
                url("/api/funcionarios/99999"), HttpMethod.GET,
                withAuth(adminToken), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void estudianteInexistente_devuelve400() {
        ResponseEntity<String> resp = rest.exchange(
                url("/api/estudiantes/99999"), HttpMethod.GET,
                withAuth(adminToken), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void seguimientoInexistente_devuelve400() {
        ResponseEntity<String> resp = rest.exchange(
                url("/api/seguimientos/99999"), HttpMethod.GET,
                withAuth(adminToken), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── CIs duplicadas → 400 ──────────────────────────────────────────────────

    @Test
    void registro_ciDuplicada_devuelve400() {
        // Primer registro
        RegistroRequestDto req = new RegistroRequestDto();
        req.setCedula(CI_DUPLICADO);
        req.setNombre("Dup");
        req.setApellido("Uno");
        req.setPassword("admin1234");
        req.setFecNacimiento(LocalDate.of(1990, 1, 1));
        req.setIdRol(1);
        ResponseEntity<LoginResponseDto> first = rest.postForEntity(url("/auth/registro"), req, LoginResponseDto.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Segundo registro con la misma CI
        req.setNombre("Dup");
        req.setApellido("Dos");
        ResponseEntity<String> second = rest.postForEntity(url("/auth/registro"), req, String.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── Campos requeridos faltantes (@Valid) → 400 ───────────────────────────

    @Test
    void instancia_sinTitulo_devuelve400() {
        InstanciaCreateDto dto = new InstanciaCreateDto();
        // titulo omitido (@NotBlank)
        dto.setTipo("Reunion");
        dto.setFecHora(OffsetDateTime.of(2025, 10, 1, 9, 0, 0, 0, ZoneOffset.ofHours(-3)));
        dto.setDescripcion("Descripción");
        dto.setIdFuncionario(adminId);

        ResponseEntity<String> resp = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(dto, adminToken), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void recordatorio_sinFecHora_devuelve400() {
        RecordatorioCreateDto dto = new RecordatorioCreateDto();
        dto.setTitulo("Recordatorio sin fecha");
        // fecHora omitida (@NotNull)

        ResponseEntity<String> resp = rest.exchange(
                url("/api/recordatorios"), HttpMethod.POST,
                withAuth(dto, adminToken), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── Incidencia duplicada para la misma instancia → 400 ───────────────────

    @Test
    void incidencia_duplicadaParaMismaInstancia_devuelve400() {
        // Crear instancia
        InstanciaCreateDto instDto = new InstanciaCreateDto();
        instDto.setTitulo("Instancia para doble incidencia");
        instDto.setTipo("Incidente");
        instDto.setFecHora(OffsetDateTime.of(2025, 11, 1, 9, 0, 0, 0, ZoneOffset.ofHours(-3)));
        instDto.setDescripcion("Descripción");
        instDto.setIdFuncionario(adminId);
        ResponseEntity<InstanciaResponseDto> instResp = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(instDto, adminToken), InstanciaResponseDto.class);
        assertThat(instResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Integer idInstancia = instResp.getBody().getIdInstancia();

        // Primera incidencia
        IncidenciaCreateDto inc = new IncidenciaCreateDto();
        inc.setIdInstancia(idInstancia);
        inc.setLugar("Aula 1");
        ResponseEntity<String> first = rest.exchange(
                url("/api/incidencias"), HttpMethod.POST,
                withAuth(inc, adminToken), String.class);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Segunda incidencia para la misma instancia
        inc.setLugar("Aula 2");
        ResponseEntity<String> second = rest.exchange(
                url("/api/incidencias"), HttpMethod.POST,
                withAuth(inc, adminToken), String.class);

        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ── Conflicto de estado → 409 ─────────────────────────────────────────────

    @Test
    void recordatorio_convertirDesactivado_devuelve409() {
        // Crear recordatorio
        RecordatorioCreateDto dto = new RecordatorioCreateDto();
        dto.setTitulo("Recordatorio a desactivar");
        dto.setFecHora(OffsetDateTime.of(2025, 12, 1, 9, 0, 0, 0, ZoneOffset.ofHours(-3)));
        ResponseEntity<RecordatorioResponseDto> creado = rest.exchange(
                url("/api/recordatorios"), HttpMethod.POST,
                withAuth(dto, adminToken), RecordatorioResponseDto.class);
        assertThat(creado.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Integer id = creado.getBody().getIdRecordatorio();

        // Desactivar el recordatorio
        rest.exchange(url("/api/recordatorios/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        // Intentar convertir el recordatorio ya inactivo
        ResponseEntity<String> resp = rest.exchange(
                url("/api/recordatorios/" + id + "/convertir-instancia"), HttpMethod.POST,
                withAuth(adminToken), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
