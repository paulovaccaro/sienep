package edu.utec.sienep;

import edu.utec.sienep.dto.EstudianteCreateDto;
import edu.utec.sienep.dto.EstudianteResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScopeIT extends BaseIT {

    private static final String CI_ADMIN     = "55555555";
    private static final String CI_PSICO     = "66666666";
    private static final String CI_EST_SUR   = "77777777";
    private static final String CI_EST_NORTE = "88888888";

    // IDs fijos del DataSeeder (create-drop H2, mismo orden de inserción)
    private static final int GRUPO_SUR   = 1; // TIC-SUR-2025-1
    private static final int GRUPO_NORTE = 3; // TIC-NOR-2025-1
    private static final int ITR_SUR     = 1;

    private String adminToken;
    private String psicoToken;
    private Integer idEstSur;
    private Integer idEstNorte;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "Scope", "admin1234", 1, null, null, null);
        psicoToken = registrarYLogin(CI_PSICO, "Psico", "Sur", "password123", 2, ITR_SUR, null, null);

        EstudianteCreateDto surDto = new EstudianteCreateDto();
        surDto.setCedula(CI_EST_SUR);
        surDto.setNombre("Estudiante");
        surDto.setApellido("Sur");
        surDto.setPassword("password123");
        surDto.setFecNacimiento(LocalDate.of(2000, 5, 10));
        surDto.setIdGrupo(GRUPO_SUR);
        ResponseEntity<EstudianteResponseDto> surResp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.POST,
                withAuth(surDto, adminToken), EstudianteResponseDto.class);
        assertThat(surResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idEstSur = surResp.getBody().getIdUsuario();

        EstudianteCreateDto norteDto = new EstudianteCreateDto();
        norteDto.setCedula(CI_EST_NORTE);
        norteDto.setNombre("Estudiante");
        norteDto.setApellido("Norte");
        norteDto.setPassword("password123");
        norteDto.setFecNacimiento(LocalDate.of(2000, 8, 20));
        norteDto.setIdGrupo(GRUPO_NORTE);
        ResponseEntity<EstudianteResponseDto> norteResp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.POST,
                withAuth(norteDto, adminToken), EstudianteResponseDto.class);
        assertThat(norteResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idEstNorte = norteResp.getBody().getIdUsuario();
    }

    @Test
    void admin_listaEstudiantes_veAmbosPorLoMenos() {
        ResponseEntity<List<EstudianteResponseDto>> resp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.GET,
                withAuth(adminToken),
                new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Integer> ids = resp.getBody().stream()
                .map(EstudianteResponseDto::getIdUsuario).toList();
        assertThat(ids).contains(idEstSur, idEstNorte);
    }

    @Test
    void admin_obtenerEstudianteNorte_devuelve200() {
        ResponseEntity<EstudianteResponseDto> resp = rest.exchange(
                url("/api/estudiantes/" + idEstNorte), HttpMethod.GET,
                withAuth(adminToken), EstudianteResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdUsuario()).isEqualTo(idEstNorte);
    }

    @Test
    void psico_listaEstudiantes_soloVeItrSur() {
        ResponseEntity<List<EstudianteResponseDto>> resp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.GET,
                withAuth(psicoToken),
                new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Integer> ids = resp.getBody().stream()
                .map(EstudianteResponseDto::getIdUsuario).toList();
        assertThat(ids).contains(idEstSur);
        assertThat(ids).doesNotContain(idEstNorte);
    }

    @Test
    void psico_obtenerEstudianteSur_devuelve200() {
        ResponseEntity<EstudianteResponseDto> resp = rest.exchange(
                url("/api/estudiantes/" + idEstSur), HttpMethod.GET,
                withAuth(psicoToken), EstudianteResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdUsuario()).isEqualTo(idEstSur);
    }

    @Test
    void psico_obtenerEstudianteNorte_devuelve403() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/estudiantes/" + idEstNorte), HttpMethod.GET,
                withAuth(psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sinToken_listarEstudiantes_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
