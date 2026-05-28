package edu.utec.sienep;

import edu.utec.sienep.dto.AsignacionCreateDto;
import edu.utec.sienep.dto.AsignacionResponseDto;
import edu.utec.sienep.dto.AsignacionUpdateDto;
import edu.utec.sienep.dto.LoginResponseDto;
import edu.utec.sienep.dto.RegistroRequestDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AsignacionControllerIT extends BaseIT {

    private static final String CI_ADMIN  = "99999999";
    private static final String CI_TARGET = "12121212"; // check=2, válida

    private String adminToken;
    private Integer targetUserId;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "Asign", "admin1234", 1, null, null, null);

        RegistroRequestDto req = new RegistroRequestDto();
        req.setCedula(CI_TARGET);
        req.setNombre("Target");
        req.setApellido("User");
        req.setPassword("password123");
        req.setFecNacimiento(LocalDate.of(1988, 4, 12));
        req.setIdRol(3); // Analista Educativo
        ResponseEntity<LoginResponseDto> resp = rest.postForEntity(
                url("/auth/registro"), req, LoginResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        targetUserId = resp.getBody().getUserId();
    }

    @Test
    void admin_crearAsignacion_devuelve201() {
        AsignacionCreateDto dto = new AsignacionCreateDto();
        dto.setIdUsuario(targetUserId);
        dto.setIdRol(2); // Psicopedagogo
        dto.setIdItr(1); // ITR Sur

        ResponseEntity<AsignacionResponseDto> resp = rest.exchange(
                url("/api/asignaciones"), HttpMethod.POST,
                withAuth(dto, adminToken), AsignacionResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getIdUsuario()).isEqualTo(targetUserId);
        assertThat(resp.getBody().getIdItr()).isEqualTo(1);
        assertThat(resp.getBody().getIdGrupo()).isNull();
    }

    @Test
    void admin_listarAsignacionesPorUsuario_devuelveListaNoVacia() {
        ResponseEntity<List<AsignacionResponseDto>> resp = rest.exchange(
                url("/api/asignaciones/usuario/" + targetUserId), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_actualizarAsignacion_modificaScope() {
        AsignacionCreateDto create = new AsignacionCreateDto();
        create.setIdUsuario(targetUserId);
        create.setIdRol(2);
        create.setIdItr(2); // ITR Norte
        ResponseEntity<AsignacionResponseDto> createResp = rest.exchange(
                url("/api/asignaciones"), HttpMethod.POST,
                withAuth(create, adminToken), AsignacionResponseDto.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Integer idAsignacion = createResp.getBody().getIdAsignacion();

        AsignacionUpdateDto update = new AsignacionUpdateDto();
        update.setEstActivo(true);
        update.setLimpiarItr(true);
        ResponseEntity<AsignacionResponseDto> updateResp = rest.exchange(
                url("/api/asignaciones/" + idAsignacion), HttpMethod.PUT,
                withAuth(update, adminToken), AsignacionResponseDto.class);

        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResp.getBody().getIdItr()).isNull();
    }

    @Test
    void admin_eliminarAsignacion_devuelve204() {
        AsignacionCreateDto create = new AsignacionCreateDto();
        create.setIdUsuario(targetUserId);
        create.setIdRol(3);
        ResponseEntity<AsignacionResponseDto> createResp = rest.exchange(
                url("/api/asignaciones"), HttpMethod.POST,
                withAuth(create, adminToken), AsignacionResponseDto.class);
        Integer idAsignacion = createResp.getBody().getIdAsignacion();

        ResponseEntity<Void> deleteResp = rest.exchange(
                url("/api/asignaciones/" + idAsignacion), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void sinToken_crearAsignacion_devuelve401() {
        AsignacionCreateDto dto = new AsignacionCreateDto();
        dto.setIdUsuario(targetUserId);
        dto.setIdRol(2);

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/asignaciones"), HttpMethod.POST,
                new HttpEntity<>(dto), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
