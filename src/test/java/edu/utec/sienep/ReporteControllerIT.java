package edu.utec.sienep;

import edu.utec.sienep.dto.EstudianteCreateDto;
import edu.utec.sienep.dto.EstudianteResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ReporteControllerIT extends BaseIT {

    private static final String CI_ADMIN    = "60000020";
    private static final String CI_STUDENT  = "40000030";

    private static final int GRUPO_SUR = 1;

    private String adminToken;
    private Integer idEstudiante;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "Rep", "admin1234", 1, null, null, null);

        EstudianteCreateDto est = new EstudianteCreateDto();
        est.setCedula(CI_STUDENT);
        est.setNombre("Est");
        est.setApellido("Reporte");
        est.setPassword("pass1234");
        est.setFecNacimiento(LocalDate.of(2000, 5, 20));
        est.setIdGrupo(GRUPO_SUR);
        ResponseEntity<EstudianteResponseDto> estResp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.POST,
                withAuth(est, adminToken), EstudianteResponseDto.class);
        assertThat(estResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idEstudiante = estResp.getBody().getIdUsuario();
    }

    @Test
    void admin_generarReporteEstudiante_devuelve200() {
        ResponseEntity<byte[]> resp = rest.exchange(
                url("/api/reportes/estudiante/" + idEstudiante), HttpMethod.GET,
                withAuth(adminToken), byte[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getHeaders().getContentType()).isNotNull();
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_generarReporteGrupo_devuelve200() {
        ResponseEntity<byte[]> resp = rest.exchange(
                url("/api/reportes/grupo/" + GRUPO_SUR), HttpMethod.GET,
                withAuth(adminToken), byte[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_generarReporteActividad_devuelve200() {
        ResponseEntity<byte[]> resp = rest.exchange(
                url("/api/reportes/actividad?fechaInicio=2025-01-01&fechaFin=2025-12-31"), HttpMethod.GET,
                withAuth(adminToken), byte[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void sinToken_generarReporte_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/reportes/grupo/" + GRUPO_SUR), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
