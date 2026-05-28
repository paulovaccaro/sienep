package edu.utec.sienep;

import edu.utec.sienep.dto.EstudianteCreateDto;
import edu.utec.sienep.dto.EstudianteResponseDto;
import edu.utec.sienep.dto.ObservacionCreateDto;
import edu.utec.sienep.dto.ObservacionResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ObservacionControllerIT extends BaseIT {

    private static final String CI_ADMIN     = "20000012";
    private static final String CI_PSICO_SUR = "30000010";
    private static final String CI_EST_SUR   = "40000018";
    private static final String CI_EST_NORTE = "50000016";

    private static final int GRUPO_SUR   = 1;
    private static final int GRUPO_NORTE = 3;
    private static final int ITR_SUR     = 1;

    private String adminToken;
    private String psicoToken;
    private Integer idEstSur;
    private Integer idEstNorte;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "Obs", "admin1234", 1, null, null, null);
        psicoToken = registrarYLogin(CI_PSICO_SUR, "Psico", "Obs", "pass1234", 2, ITR_SUR, null, null);

        EstudianteCreateDto surDto = new EstudianteCreateDto();
        surDto.setCedula(CI_EST_SUR);
        surDto.setNombre("Est");
        surDto.setApellido("SurObs");
        surDto.setPassword("pass1234");
        surDto.setFecNacimiento(LocalDate.of(2002, 4, 15));
        surDto.setIdGrupo(GRUPO_SUR);
        ResponseEntity<EstudianteResponseDto> surResp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.POST,
                withAuth(surDto, adminToken), EstudianteResponseDto.class);
        assertThat(surResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idEstSur = surResp.getBody().getIdUsuario();

        EstudianteCreateDto norteDto = new EstudianteCreateDto();
        norteDto.setCedula(CI_EST_NORTE);
        norteDto.setNombre("Est");
        norteDto.setApellido("NorteObs");
        norteDto.setPassword("pass1234");
        norteDto.setFecNacimiento(LocalDate.of(2002, 7, 25));
        norteDto.setIdGrupo(GRUPO_NORTE);
        ResponseEntity<EstudianteResponseDto> norteResp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.POST,
                withAuth(norteDto, adminToken), EstudianteResponseDto.class);
        assertThat(norteResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idEstNorte = norteResp.getBody().getIdUsuario();
    }

    private ObservacionCreateDto obs(Integer idEstudiante) {
        ObservacionCreateDto dto = new ObservacionCreateDto();
        dto.setIdEstudiante(idEstudiante);
        dto.setTitulo("Observación de test");
        dto.setContenido("Contenido de prueba");
        return dto;
    }

    @Test
    void admin_crearObservacion_devuelve201() {
        ResponseEntity<ObservacionResponseDto> resp = rest.exchange(
                url("/api/observaciones"), HttpMethod.POST,
                withAuth(obs(idEstSur), adminToken), ObservacionResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getIdEstudiante()).isEqualTo(idEstSur);
    }

    @Test
    void psico_crearObservacionEnSuScope_devuelve201() {
        ResponseEntity<ObservacionResponseDto> resp = rest.exchange(
                url("/api/observaciones"), HttpMethod.POST,
                withAuth(obs(idEstSur), psicoToken), ObservacionResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void psico_crearObservacionFueraDeScope_devuelve403() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/observaciones"), HttpMethod.POST,
                withAuth(obs(idEstNorte), psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void admin_listarObservacionesPorEstudiante_devuelve200() {
        rest.exchange(url("/api/observaciones"), HttpMethod.POST,
                withAuth(obs(idEstSur), adminToken), ObservacionResponseDto.class);

        ResponseEntity<List<ObservacionResponseDto>> resp = rest.exchange(
                url("/api/observaciones/estudiante/" + idEstSur), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        resp.getBody().forEach(o -> assertThat(o.getIdEstudiante()).isEqualTo(idEstSur));
    }

    @Test
    void psico_listarObservacionesEstudianteOtroItr_devuelve403() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/observaciones/estudiante/" + idEstNorte), HttpMethod.GET,
                withAuth(psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void admin_obtenerObservacion_devuelve200() {
        ResponseEntity<ObservacionResponseDto> creada = rest.exchange(
                url("/api/observaciones"), HttpMethod.POST,
                withAuth(obs(idEstSur), adminToken), ObservacionResponseDto.class);
        Integer id = creada.getBody().getIdObservacion();

        ResponseEntity<ObservacionResponseDto> resp = rest.exchange(
                url("/api/observaciones/" + id), HttpMethod.GET,
                withAuth(adminToken), ObservacionResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdObservacion()).isEqualTo(id);
    }

    @Test
    void admin_desactivarObservacion_devuelve204() {
        ResponseEntity<ObservacionResponseDto> creada = rest.exchange(
                url("/api/observaciones"), HttpMethod.POST,
                withAuth(obs(idEstSur), adminToken), ObservacionResponseDto.class);
        Integer id = creada.getBody().getIdObservacion();

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/observaciones/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void sinToken_crearObservacion_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/observaciones"), HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(obs(idEstSur)), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
