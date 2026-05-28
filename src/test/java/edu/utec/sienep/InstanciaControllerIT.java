package edu.utec.sienep;

import edu.utec.sienep.dto.InstanciaClonarDto;
import edu.utec.sienep.dto.InstanciaCreateDto;
import edu.utec.sienep.dto.InstanciaResponseDto;
import edu.utec.sienep.dto.LoginResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InstanciaControllerIT extends BaseIT {

    private static final String CI_ADMIN = "80000010";
    private static final String CI_PSICO = "90000018";

    private String adminToken;
    private Integer adminId;
    private String psicoToken;
    private Integer psicoId;

    @BeforeAll
    void setup() {
        LoginResponseDto adminResp = registrarConId(CI_ADMIN, "Admin", "Inst", "admin1234", 1, null);
        adminToken = adminResp.getToken();
        adminId    = adminResp.getUserId();

        // sin scope → asignación global → tienePermisoGlobal funciona para instancias.crear
        LoginResponseDto psicoResp = registrarConId(CI_PSICO, "Psico", "Inst", "pass1234", 2, null);
        psicoToken = psicoResp.getToken();
        psicoId    = psicoResp.getUserId();
    }

    private LoginResponseDto registrarConId(String cedula, String nombre, String apellido,
                                            String password, Integer idRol, Integer idItr) {
        edu.utec.sienep.dto.RegistroRequestDto req = new edu.utec.sienep.dto.RegistroRequestDto();
        req.setCedula(cedula);
        req.setNombre(nombre);
        req.setApellido(apellido);
        req.setPassword(password);
        req.setFecNacimiento(LocalDate.of(1985, 1, 1));
        req.setIdRol(idRol);
        req.setIdItr(idItr);
        ResponseEntity<LoginResponseDto> resp = rest.postForEntity(url("/auth/registro"), req, LoginResponseDto.class);
        if (!resp.getStatusCode().is2xxSuccessful())
            throw new IllegalStateException("Registro falló: " + cedula);
        return resp.getBody();
    }

    private InstanciaCreateDto instancia(Integer idFuncionario) {
        InstanciaCreateDto dto = new InstanciaCreateDto();
        dto.setTitulo("Instancia de test");
        dto.setTipo("Reunion");
        dto.setFecHora(OffsetDateTime.of(2025, 8, 15, 10, 0, 0, 0, ZoneOffset.ofHours(-3)));
        dto.setDescripcion("Descripción de prueba");
        dto.setIdFuncionario(idFuncionario);
        return dto;
    }

    @Test
    void admin_crearInstancia_devuelve201() {
        ResponseEntity<InstanciaResponseDto> resp = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(instancia(adminId), adminToken), InstanciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getTitulo()).isEqualTo("Instancia de test");
        assertThat(resp.getBody().getIdFuncionario()).isEqualTo(adminId);
    }

    @Test
    void admin_listarInstancias_devuelve200() {
        rest.exchange(url("/api/instancias"), HttpMethod.POST,
                withAuth(instancia(adminId), adminToken), InstanciaResponseDto.class);

        ResponseEntity<List<InstanciaResponseDto>> resp = rest.exchange(
                url("/api/instancias"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_obtenerInstancia_devuelve200() {
        ResponseEntity<InstanciaResponseDto> creada = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(instancia(adminId), adminToken), InstanciaResponseDto.class);
        Integer id = creada.getBody().getIdInstancia();

        ResponseEntity<InstanciaResponseDto> resp = rest.exchange(
                url("/api/instancias/" + id), HttpMethod.GET,
                withAuth(adminToken), InstanciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdInstancia()).isEqualTo(id);
    }

    @Test
    void admin_actualizarInstancia_devuelve200() {
        ResponseEntity<InstanciaResponseDto> creada = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(instancia(adminId), adminToken), InstanciaResponseDto.class);
        Integer id = creada.getBody().getIdInstancia();

        InstanciaCreateDto upd = instancia(adminId);
        upd.setTitulo("Instancia actualizada");
        ResponseEntity<InstanciaResponseDto> resp = rest.exchange(
                url("/api/instancias/" + id), HttpMethod.PUT,
                withAuth(upd, adminToken), InstanciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getTitulo()).isEqualTo("Instancia actualizada");
    }

    @Test
    void admin_clonarInstancia_devuelve201() {
        ResponseEntity<InstanciaResponseDto> creada = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(instancia(adminId), adminToken), InstanciaResponseDto.class);
        Integer id = creada.getBody().getIdInstancia();

        // enviar body vacío explícito para que Spring resuelva @RequestBody(required = false)
        InstanciaClonarDto cuerpoVacio = new InstanciaClonarDto();
        ResponseEntity<InstanciaResponseDto> resp = rest.exchange(
                url("/api/instancias/" + id + "/clonar"), HttpMethod.POST,
                withAuth(cuerpoVacio, adminToken), InstanciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getTitulo()).isEqualTo("Instancia de test");
        assertThat(resp.getBody().getIdInstancia()).isNotEqualTo(id);
    }

    @Test
    void admin_desactivarInstancia_devuelve204() {
        ResponseEntity<InstanciaResponseDto> creada = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(instancia(adminId), adminToken), InstanciaResponseDto.class);
        Integer id = creada.getBody().getIdInstancia();

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/instancias/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void psico_crearInstancia_devuelve201() {
        // Psicopedagogo tiene instancias.crear
        ResponseEntity<InstanciaResponseDto> resp = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(instancia(psicoId), psicoToken), InstanciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void psico_eliminarInstancia_devuelve403() {
        // Psicopedagogo no tiene instancias.eliminar
        ResponseEntity<InstanciaResponseDto> creada = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(instancia(adminId), adminToken), InstanciaResponseDto.class);
        Integer id = creada.getBody().getIdInstancia();

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/instancias/" + id), HttpMethod.DELETE,
                withAuth(psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sinToken_listarInstancias_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/instancias"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
