package edu.utec.sienep;

import edu.utec.sienep.dto.EstudianteCreateDto;
import edu.utec.sienep.dto.EstudianteResponseDto;
import edu.utec.sienep.dto.SeguimientoCreateDto;
import edu.utec.sienep.dto.SeguimientoResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeguimientoControllerIT extends BaseIT {

    private static final String CI_ADMIN      = "70000006";
    private static final String CI_PSICO_SUR  = "80000004";
    private static final String CI_EST_SUR    = "90000002";
    private static final String CI_EST_NORTE  = "10000014";

    private static final int GRUPO_SUR   = 1;
    private static final int GRUPO_NORTE = 3;
    private static final int ITR_SUR     = 1;

    private String adminToken;
    private String psicoToken;
    private Integer idEstSur;
    private Integer idEstNorte;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "Seg", "admin1234", 1, null, null, null);
        psicoToken = registrarYLogin(CI_PSICO_SUR, "Psico", "Sur", "pass1234", 2, ITR_SUR, null, null);

        EstudianteCreateDto surDto = new EstudianteCreateDto();
        surDto.setCedula(CI_EST_SUR);
        surDto.setNombre("Est");
        surDto.setApellido("Sur");
        surDto.setPassword("pass1234");
        surDto.setFecNacimiento(LocalDate.of(2001, 3, 10));
        surDto.setIdGrupo(GRUPO_SUR);
        ResponseEntity<EstudianteResponseDto> surResp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.POST,
                withAuth(surDto, adminToken), EstudianteResponseDto.class);
        assertThat(surResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idEstSur = surResp.getBody().getIdUsuario();

        EstudianteCreateDto norteDto = new EstudianteCreateDto();
        norteDto.setCedula(CI_EST_NORTE);
        norteDto.setNombre("Est");
        norteDto.setApellido("Norte");
        norteDto.setPassword("pass1234");
        norteDto.setFecNacimiento(LocalDate.of(2001, 5, 20));
        norteDto.setIdGrupo(GRUPO_NORTE);
        ResponseEntity<EstudianteResponseDto> norteResp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.POST,
                withAuth(norteDto, adminToken), EstudianteResponseDto.class);
        assertThat(norteResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idEstNorte = norteResp.getBody().getIdUsuario();
    }

    private SeguimientoCreateDto crearDto(Integer idEstudiante) {
        SeguimientoCreateDto dto = new SeguimientoCreateDto();
        dto.setIdEstudiante(idEstudiante);
        dto.setFecInicio(LocalDate.of(2025, 1, 1));
        return dto;
    }

    @Test
    void admin_crearSeguimiento_devuelve201() {
        ResponseEntity<SeguimientoResponseDto> resp = rest.exchange(
                url("/api/seguimientos"), HttpMethod.POST,
                withAuth(crearDto(idEstSur), adminToken), SeguimientoResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getIdEstudiante()).isEqualTo(idEstSur);
    }

    @Test
    void psico_crearSeguimientoEnSuScope_devuelve201() {
        ResponseEntity<SeguimientoResponseDto> resp = rest.exchange(
                url("/api/seguimientos"), HttpMethod.POST,
                withAuth(crearDto(idEstSur), psicoToken), SeguimientoResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void psico_crearSeguimientoFueraDeScope_devuelve403() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/seguimientos"), HttpMethod.POST,
                withAuth(crearDto(idEstNorte), psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void admin_listarSeguimientos_devuelve200() {
        // Crear uno primero para asegurar que hay al menos uno
        rest.exchange(url("/api/seguimientos"), HttpMethod.POST,
                withAuth(crearDto(idEstSur), adminToken), SeguimientoResponseDto.class);

        ResponseEntity<List<SeguimientoResponseDto>> resp = rest.exchange(
                url("/api/seguimientos"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void psico_listarSeguimientos_soloVeLosDeSuScope() {
        rest.exchange(url("/api/seguimientos"), HttpMethod.POST,
                withAuth(crearDto(idEstSur), adminToken), SeguimientoResponseDto.class);

        ResponseEntity<List<SeguimientoResponseDto>> resp = rest.exchange(
                url("/api/seguimientos"), HttpMethod.GET,
                withAuth(psicoToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        resp.getBody().forEach(s -> assertThat(s.getIdGrupo()).isIn(GRUPO_SUR, 2)); // grupos del ITR Sur
    }

    @Test
    void admin_obtenerSeguimientoPorId_devuelve200() {
        ResponseEntity<SeguimientoResponseDto> creado = rest.exchange(
                url("/api/seguimientos"), HttpMethod.POST,
                withAuth(crearDto(idEstSur), adminToken), SeguimientoResponseDto.class);
        Integer id = creado.getBody().getIdSeguimiento();

        ResponseEntity<SeguimientoResponseDto> resp = rest.exchange(
                url("/api/seguimientos/" + id), HttpMethod.GET,
                withAuth(adminToken), SeguimientoResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdSeguimiento()).isEqualTo(id);
    }

    @Test
    void psico_obtenerSeguimientoFueraDeScope_devuelve403() {
        // Admin crea un seguimiento para estNorte
        ResponseEntity<SeguimientoResponseDto> creado = rest.exchange(
                url("/api/seguimientos"), HttpMethod.POST,
                withAuth(crearDto(idEstNorte), adminToken), SeguimientoResponseDto.class);
        Integer id = creado.getBody().getIdSeguimiento();

        // Psico del ITR Sur intenta obtenerlo
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/seguimientos/" + id), HttpMethod.GET,
                withAuth(psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void admin_listarSeguimientosPorEstudiante_devuelve200() {
        rest.exchange(url("/api/seguimientos"), HttpMethod.POST,
                withAuth(crearDto(idEstSur), adminToken), SeguimientoResponseDto.class);

        ResponseEntity<List<SeguimientoResponseDto>> resp = rest.exchange(
                url("/api/seguimientos/estudiante/" + idEstSur), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        resp.getBody().forEach(s -> assertThat(s.getIdEstudiante()).isEqualTo(idEstSur));
    }

    @Test
    void admin_actualizarSeguimiento_devuelve200() {
        ResponseEntity<SeguimientoResponseDto> creado = rest.exchange(
                url("/api/seguimientos"), HttpMethod.POST,
                withAuth(crearDto(idEstSur), adminToken), SeguimientoResponseDto.class);
        Integer id = creado.getBody().getIdSeguimiento();

        SeguimientoCreateDto upd = crearDto(idEstSur);
        upd.setFecCierre(LocalDate.of(2025, 6, 30));
        ResponseEntity<SeguimientoResponseDto> resp = rest.exchange(
                url("/api/seguimientos/" + id), HttpMethod.PUT,
                withAuth(upd, adminToken), SeguimientoResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getFecCierre()).isEqualTo(LocalDate.of(2025, 6, 30));
    }

    @Test
    void admin_eliminarSeguimiento_devuelve204() {
        ResponseEntity<SeguimientoResponseDto> creado = rest.exchange(
                url("/api/seguimientos"), HttpMethod.POST,
                withAuth(crearDto(idEstSur), adminToken), SeguimientoResponseDto.class);
        Integer id = creado.getBody().getIdSeguimiento();

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/seguimientos/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void sinToken_crearSeguimiento_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/seguimientos"), HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(crearDto(idEstSur)), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
