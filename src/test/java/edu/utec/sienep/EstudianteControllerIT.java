package edu.utec.sienep;

import edu.utec.sienep.dto.EstudianteCreateDto;
import edu.utec.sienep.dto.EstudianteResponseDto;
import edu.utec.sienep.dto.EstudianteUpdateDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EstudianteControllerIT extends BaseIT {

    private static final String CI_ADMIN     = "50000038";
    private static final String CI_PSICO_SUR = "60000036";
    private static final String CI_EST_SUR   = "70000034";
    private static final String CI_EST_NUEVO = "80000032";
    private static final String CI_EST_PSICO = "90000030";
    private static final String CI_EST_BORRAR = "20000040";

    private static final int ITR_SUR    = 1;
    private static final int GRUPO_SUR  = 1;
    private static final int GRUPO_NORTE = 3;

    private String adminToken;
    private String psicoToken;
    private Integer idEstSur;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "Est", "admin1234", 1, null, null, null);
        psicoToken = registrarYLogin(CI_PSICO_SUR, "Psico", "Sur", "pass1234", 2, ITR_SUR, null, null);

        idEstSur = crearEstudiante(CI_EST_SUR, "Est", "Sur", GRUPO_SUR, adminToken);
    }

    private Integer crearEstudiante(String cedula, String nombre, String apellido,
                                    int idGrupo, String token) {
        EstudianteCreateDto dto = new EstudianteCreateDto();
        dto.setCedula(cedula);
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        dto.setPassword("pass1234");
        dto.setFecNacimiento(LocalDate.of(2001, 6, 15));
        dto.setIdGrupo(idGrupo);
        ResponseEntity<EstudianteResponseDto> resp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.POST,
                withAuth(dto, token), EstudianteResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().getIdUsuario();
    }

    @Test
    void admin_crearEstudiante_devuelve201() {
        EstudianteCreateDto dto = new EstudianteCreateDto();
        dto.setCedula(CI_EST_NUEVO);
        dto.setNombre("Nuevo");
        dto.setApellido("Estudiante");
        dto.setPassword("pass1234");
        dto.setFecNacimiento(LocalDate.of(2002, 3, 10));
        dto.setIdGrupo(GRUPO_SUR);

        ResponseEntity<EstudianteResponseDto> resp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.POST,
                withAuth(dto, adminToken), EstudianteResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getCedula()).isEqualTo(CI_EST_NUEVO);
        assertThat(resp.getBody().getIdGrupo()).isEqualTo(GRUPO_SUR);
    }

    @Test
    void admin_listarEstudiantes_devuelve200() {
        ResponseEntity<List<EstudianteResponseDto>> resp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_obtenerEstudiante_devuelve200() {
        ResponseEntity<EstudianteResponseDto> resp = rest.exchange(
                url("/api/estudiantes/" + idEstSur), HttpMethod.GET,
                withAuth(adminToken), EstudianteResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdUsuario()).isEqualTo(idEstSur);
    }

    @Test
    void admin_actualizarEstudiante_devuelve200() {
        EstudianteUpdateDto upd = new EstudianteUpdateDto();
        upd.setNombre("EstudianteActualizado");

        ResponseEntity<EstudianteResponseDto> resp = rest.exchange(
                url("/api/estudiantes/" + idEstSur), HttpMethod.PUT,
                withAuth(upd, adminToken), EstudianteResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getNombre()).isEqualTo("EstudianteActualizado");
    }

    @Test
    void admin_desactivarEstudiante_devuelve204() {
        Integer id = crearEstudiante(CI_EST_BORRAR, "Para", "Borrar", GRUPO_SUR, adminToken);

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/estudiantes/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void psico_listarEstudiantesEnSuScope_devuelve200() {
        // Psico con ITR_SUR solo ve grupos de su ITR
        ResponseEntity<List<EstudianteResponseDto>> resp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.GET,
                withAuth(psicoToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        resp.getBody().forEach(e -> assertThat(e.getIdGrupo()).isIn(GRUPO_SUR, 2));
    }

    @Test
    void psico_crearEstudiante_devuelve403() {
        // Psicopedagogo no tiene estudiantes.crear
        EstudianteCreateDto dto = new EstudianteCreateDto();
        dto.setCedula(CI_EST_PSICO);
        dto.setNombre("Est");
        dto.setApellido("Psico");
        dto.setPassword("pass1234");
        dto.setFecNacimiento(LocalDate.of(2003, 1, 20));
        dto.setIdGrupo(GRUPO_SUR);

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.POST,
                withAuth(dto, psicoToken), Void.class);

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
