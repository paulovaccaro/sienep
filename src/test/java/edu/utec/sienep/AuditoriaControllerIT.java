package edu.utec.sienep;

import edu.utec.sienep.dto.AuditoriaResponseDto;
import edu.utec.sienep.dto.LoginResponseDto;
import edu.utec.sienep.dto.RegistroRequestDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Auditoría y trazabilidad – requisito no funcional.
 * GET /api/auditoria (todos), /entidad/{e}/{id}, /usuario/{id}.
 * CIs: 22000008 (admin), 23000009 (psico sin auditoria.leer).
 */
class AuditoriaControllerIT extends BaseIT {

    private static final String CI_ADMIN = "22000008";
    private static final String CI_PSICO = "23000009";

    private String adminToken;
    private Integer adminId;
    private String psicoToken;

    @BeforeAll
    void setup() {
        LoginResponseDto adminResp = registrar(CI_ADMIN, "Audit", "Admin", "admin1234", 1);
        adminId = adminResp.getUserId();
        // Login explícito para generar audit LOGIN con id_usuario commiteado
        adminToken = login(adminResp.getUsername(), "admin1234");

        LoginResponseDto psicoResp = registrar(CI_PSICO, "Audit", "Psico", "pass1234", 2);
        psicoToken = psicoResp.getToken();
    }

    private LoginResponseDto registrar(String cedula, String nombre, String apellido,
                                       String password, int idRol) {
        RegistroRequestDto req = new RegistroRequestDto();
        req.setCedula(cedula);
        req.setNombre(nombre);
        req.setApellido(apellido);
        req.setPassword(password);
        req.setFecNacimiento(LocalDate.of(1985, 6, 15));
        req.setIdRol(idRol);
        ResponseEntity<LoginResponseDto> resp = rest.postForEntity(
                url("/auth/registro"), req, LoginResponseDto.class);
        if (!resp.getStatusCode().is2xxSuccessful())
            throw new IllegalStateException("Registro falló: " + cedula);
        return resp.getBody();
    }

    // ── GET /api/auditoria ────────────────────────────────────────────────────

    @Test
    void admin_listarAuditoria_devuelve200ConRegistros() {
        ResponseEntity<List<AuditoriaResponseDto>> resp = rest.exchange(
                url("/api/auditoria"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        // El setup() genera registros de auditoría (CREAR funcionarios, LOGIN)
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_listarAuditoria_registrosTienenCamposEsperados() {
        ResponseEntity<List<AuditoriaResponseDto>> resp = rest.exchange(
                url("/api/auditoria"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuditoriaResponseDto primero = resp.getBody().get(0);
        assertThat(primero.getIdAuditoria()).isNotNull();
        assertThat(primero.getAccion()).isNotBlank();
        assertThat(primero.getEntidad()).isNotBlank();
        assertThat(primero.getFecHora()).isNotNull();
    }

    @Test
    void psico_listarAuditoria_devuelve403() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/auditoria"), HttpMethod.GET,
                withAuth(psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sinToken_listarAuditoria_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/auditoria"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── GET /api/auditoria/entidad/{entidad}/{idEntidad} ──────────────────────

    @Test
    void admin_filtrarPorEntidad_devuelve200() {
        ResponseEntity<List<AuditoriaResponseDto>> resp = rest.exchange(
                url("/api/auditoria/entidad/funcionarios/" + adminId), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
        resp.getBody().forEach(a -> assertThat(a.getEntidad()).isEqualTo("funcionarios"));
    }

    @Test
    void admin_filtrarPorEntidadInexistente_devuelve200ListaVacia() {
        ResponseEntity<List<AuditoriaResponseDto>> resp = rest.exchange(
                url("/api/auditoria/entidad/inexistente/999"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEmpty();
    }

    // ── GET /api/auditoria/usuario/{idUsuario} ────────────────────────────────

    @Test
    void admin_filtrarPorUsuario_devuelve200() {
        ResponseEntity<List<AuditoriaResponseDto>> resp = rest.exchange(
                url("/api/auditoria/usuario/" + adminId), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
        resp.getBody().forEach(a -> assertThat(a.getIdUsuario()).isEqualTo(adminId));
    }

    @Test
    void psico_filtrarPorEntidad_devuelve403() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/auditoria/entidad/funcionarios/1"), HttpMethod.GET,
                withAuth(psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
