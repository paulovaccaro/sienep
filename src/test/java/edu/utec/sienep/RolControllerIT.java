package edu.utec.sienep;

import edu.utec.sienep.dto.LoginResponseDto;
import edu.utec.sienep.dto.RolCreateDto;
import edu.utec.sienep.dto.RolResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RF32 – Gestión de roles / RF34 – Roles personalizados.
 * CIs: 15000003 (admin), 16000004 (psico sin roles.gestionar).
 */
class RolControllerIT extends BaseIT {

    private static final String CI_ADMIN = "15000003";
    private static final String CI_PSICO = "16000004";

    private String adminToken;
    private String psicoToken;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "Rol", "admin1234", 1, null, null, null);
        psicoToken = registrarYLogin(CI_PSICO, "Psico", "Rol", "pass1234", 2, null, null, null);
    }

    // ── helpers ────────────────────────────────────────────────────────────

    private RolResponseDto crearRol(String nombre, String descripcion) {
        RolCreateDto dto = new RolCreateDto();
        dto.setNombre(nombre);
        dto.setDescripcion(descripcion);
        ResponseEntity<RolResponseDto> resp = rest.exchange(
                url("/api/roles"), HttpMethod.POST,
                withAuth(dto, adminToken), RolResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody();
    }

    // ── listado ────────────────────────────────────────────────────────────

    @Test
    void admin_listarRoles_devuelve200ConRolesPreexistentes() {
        ResponseEntity<List<RolResponseDto>> resp = rest.exchange(
                url("/api/roles"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        // El DataSeeder crea 6 roles de sistema
        assertThat(resp.getBody()).hasSizeGreaterThanOrEqualTo(6);
        assertThat(resp.getBody()).anyMatch(r -> r.getNombre().equals("Administrador"));
        assertThat(resp.getBody()).anyMatch(r -> r.getNombre().equals("Psicopedagogo"));
    }

    @Test
    void psico_listarRoles_devuelve403() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/roles"), HttpMethod.GET,
                withAuth(psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sinToken_listarRoles_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/roles"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── obtener por ID ─────────────────────────────────────────────────────

    @Test
    void admin_obtenerRolPorId_devuelve200() {
        ResponseEntity<List<RolResponseDto>> lista = rest.exchange(
                url("/api/roles"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});
        Integer id = lista.getBody().get(0).getIdRol();

        ResponseEntity<RolResponseDto> resp = rest.exchange(
                url("/api/roles/" + id), HttpMethod.GET,
                withAuth(adminToken), RolResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdRol()).isEqualTo(id);
    }

    // ── creación (RF34) ────────────────────────────────────────────────────

    @Test
    void admin_crearRolPersonalizado_devuelve201() {
        RolCreateDto dto = new RolCreateDto();
        dto.setNombre("Tutor Académico");
        dto.setDescripcion("Rol personalizado para tutores");

        ResponseEntity<RolResponseDto> resp = rest.exchange(
                url("/api/roles"), HttpMethod.POST,
                withAuth(dto, adminToken), RolResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getNombre()).isEqualTo("Tutor Académico");
        assertThat(resp.getBody().getEsSistema()).isFalse();
        assertThat(resp.getBody().getEstActivo()).isTrue();
    }

    @Test
    void admin_crearRolDuplicado_devuelve409() {
        // primer intento
        crearRol("RolDuplicado", "desc");

        // segundo intento con mismo nombre
        RolCreateDto dto = new RolCreateDto();
        dto.setNombre("RolDuplicado");
        ResponseEntity<String> resp = rest.exchange(
                url("/api/roles"), HttpMethod.POST,
                withAuth(dto, adminToken), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void psico_crearRol_devuelve403() {
        RolCreateDto dto = new RolCreateDto();
        dto.setNombre("RolForbidden");

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/roles"), HttpMethod.POST,
                withAuth(dto, psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // ── actualización (RF32) ───────────────────────────────────────────────

    @Test
    void admin_actualizarRolPersonalizado_devuelve200() {
        RolResponseDto creado = crearRol("RolParaEditar", "descripción inicial");

        RolCreateDto upd = new RolCreateDto();
        upd.setNombre("RolEditado");
        upd.setDescripcion("descripción actualizada");

        ResponseEntity<RolResponseDto> resp = rest.exchange(
                url("/api/roles/" + creado.getIdRol()), HttpMethod.PUT,
                withAuth(upd, adminToken), RolResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getNombre()).isEqualTo("RolEditado");
    }

    // ── desactivación (RF32) ───────────────────────────────────────────────

    @Test
    void admin_desactivarRolPersonalizado_devuelve204() {
        RolResponseDto creado = crearRol("RolParaBorrar", "se desactiva");

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/roles/" + creado.getIdRol()), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void admin_desactivarRolDeSistema_devuelve409() {
        // Administrador (id=1) es un rol de sistema
        ResponseEntity<List<RolResponseDto>> lista = rest.exchange(
                url("/api/roles"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});
        Integer idSistema = lista.getBody().stream()
                .filter(r -> Boolean.TRUE.equals(r.getEsSistema()))
                .findFirst()
                .map(RolResponseDto::getIdRol)
                .orElseThrow();

        ResponseEntity<String> resp = rest.exchange(
                url("/api/roles/" + idSistema), HttpMethod.DELETE,
                withAuth(adminToken), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
