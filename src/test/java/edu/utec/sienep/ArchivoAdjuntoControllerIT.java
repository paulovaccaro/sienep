package edu.utec.sienep;

import edu.utec.sienep.dto.ArchivoAdjuntoCreateDto;
import edu.utec.sienep.dto.ArchivoAdjuntoResponseDto;
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

class ArchivoAdjuntoControllerIT extends BaseIT {

    private static final String CI_ADMIN     = "70000028";
    private static final String CI_PSICO_SUR = "80000026";
    private static final String CI_EST_SUR   = "90000024";
    private static final String CI_EST_NORTE = "10000036";

    private static final int ITR_SUR    = 1;
    private static final int GRUPO_SUR  = 1;
    private static final int GRUPO_NORTE = 3;

    private String adminToken;
    private String psicoToken;
    private Integer idEstSur;
    private Integer idEstNorte;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "Arch", "admin1234", 1, null, null, null);
        psicoToken = registrarYLogin(CI_PSICO_SUR, "Psico", "Sur", "pass1234", 2, ITR_SUR, null, null);

        idEstSur = crearEstudiante(CI_EST_SUR, "Est", "Sur", GRUPO_SUR);
        idEstNorte = crearEstudiante(CI_EST_NORTE, "Est", "Norte", GRUPO_NORTE);
    }

    private Integer crearEstudiante(String cedula, String nombre, String apellido, int idGrupo) {
        EstudianteCreateDto dto = new EstudianteCreateDto();
        dto.setCedula(cedula);
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        dto.setPassword("pass1234");
        dto.setFecNacimiento(LocalDate.of(2001, 6, 15));
        dto.setIdGrupo(idGrupo);
        ResponseEntity<EstudianteResponseDto> resp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.POST,
                withAuth(dto, adminToken), EstudianteResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().getIdUsuario();
    }

    private ArchivoAdjuntoCreateDto archivoDto(Integer idEstudiante) {
        ArchivoAdjuntoCreateDto dto = new ArchivoAdjuntoCreateDto();
        dto.setIdEstudiante(idEstudiante);
        dto.setRuta("/archivos/doc-" + idEstudiante + ".pdf");
        dto.setCategoria("Certificado");
        return dto;
    }

    private Integer crearArchivo(Integer idEstudiante, String token) {
        ResponseEntity<ArchivoAdjuntoResponseDto> resp = rest.exchange(
                url("/api/archivos"), HttpMethod.POST,
                withAuth(archivoDto(idEstudiante), token), ArchivoAdjuntoResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().getIdArchivoAdjunto();
    }

    @Test
    void admin_crearArchivo_devuelve201() {
        ResponseEntity<ArchivoAdjuntoResponseDto> resp = rest.exchange(
                url("/api/archivos"), HttpMethod.POST,
                withAuth(archivoDto(idEstSur), adminToken), ArchivoAdjuntoResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getIdEstudiante()).isEqualTo(idEstSur);
        assertThat(resp.getBody().getEstActivo()).isTrue();
    }

    @Test
    void admin_listarArchivosPorEstudiante_devuelve200() {
        crearArchivo(idEstSur, adminToken);

        ResponseEntity<List<ArchivoAdjuntoResponseDto>> resp = rest.exchange(
                url("/api/archivos/estudiante/" + idEstSur), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_obtenerArchivo_devuelve200() {
        Integer id = crearArchivo(idEstSur, adminToken);

        ResponseEntity<ArchivoAdjuntoResponseDto> resp = rest.exchange(
                url("/api/archivos/" + id), HttpMethod.GET,
                withAuth(adminToken), ArchivoAdjuntoResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdArchivoAdjunto()).isEqualTo(id);
    }

    @Test
    void admin_desactivarArchivo_devuelve204() {
        Integer id = crearArchivo(idEstSur, adminToken);

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/archivos/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void psico_crearArchivoEnSuScope_devuelve201() {
        ResponseEntity<ArchivoAdjuntoResponseDto> resp = rest.exchange(
                url("/api/archivos"), HttpMethod.POST,
                withAuth(archivoDto(idEstSur), psicoToken), ArchivoAdjuntoResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void psico_crearArchivoFueraDeScope_devuelve403() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/archivos"), HttpMethod.POST,
                withAuth(archivoDto(idEstNorte), psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sinToken_crearArchivo_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/archivos"), HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(archivoDto(idEstSur)), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
