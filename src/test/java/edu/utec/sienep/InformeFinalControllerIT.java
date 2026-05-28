package edu.utec.sienep;

import edu.utec.sienep.dto.InformeFinalCreateDto;
import edu.utec.sienep.dto.InformeFinalResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InformeFinalControllerIT extends BaseIT {

    private static final String CI_ADMIN = "60000014";
    private static final String CI_PSICO = "70000012";

    private String adminToken;
    private String psicoToken;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "Inf", "admin1234", 1, null, null, null);
        // sin scope → asignación global → tienePermisoGlobal funciona para info_final.crear
        psicoToken = registrarYLogin(CI_PSICO, "Psico", "Inf", "pass1234", 2, null, null, null);
    }

    private InformeFinalCreateDto informe() {
        InformeFinalCreateDto dto = new InformeFinalCreateDto();
        dto.setContenido("Contenido del informe final de prueba");
        dto.setValoracion(8);
        dto.setFecCreacion(LocalDate.of(2025, 6, 1));
        return dto;
    }

    @Test
    void admin_crearInforme_devuelve201() {
        ResponseEntity<InformeFinalResponseDto> resp = rest.exchange(
                url("/api/informes"), HttpMethod.POST,
                withAuth(informe(), adminToken), InformeFinalResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getValoracion()).isEqualTo(8);
        assertThat(resp.getBody().getEstActivo()).isTrue();
    }

    @Test
    void admin_listarInformes_devuelve200() {
        rest.exchange(url("/api/informes"), HttpMethod.POST,
                withAuth(informe(), adminToken), InformeFinalResponseDto.class);

        ResponseEntity<List<InformeFinalResponseDto>> resp = rest.exchange(
                url("/api/informes"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_obtenerInforme_devuelve200() {
        ResponseEntity<InformeFinalResponseDto> creado = rest.exchange(
                url("/api/informes"), HttpMethod.POST,
                withAuth(informe(), adminToken), InformeFinalResponseDto.class);
        Integer id = creado.getBody().getIdInfFinal();

        ResponseEntity<InformeFinalResponseDto> resp = rest.exchange(
                url("/api/informes/" + id), HttpMethod.GET,
                withAuth(adminToken), InformeFinalResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdInfFinal()).isEqualTo(id);
    }

    @Test
    void admin_actualizarInforme_devuelve200() {
        ResponseEntity<InformeFinalResponseDto> creado = rest.exchange(
                url("/api/informes"), HttpMethod.POST,
                withAuth(informe(), adminToken), InformeFinalResponseDto.class);
        Integer id = creado.getBody().getIdInfFinal();

        InformeFinalCreateDto upd = new InformeFinalCreateDto();
        upd.setContenido("Contenido actualizado");
        upd.setValoracion(10);
        ResponseEntity<InformeFinalResponseDto> resp = rest.exchange(
                url("/api/informes/" + id), HttpMethod.PUT,
                withAuth(upd, adminToken), InformeFinalResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getValoracion()).isEqualTo(10);
        assertThat(resp.getBody().getContenido()).isEqualTo("Contenido actualizado");
    }

    @Test
    void admin_desactivarInforme_devuelve204() {
        ResponseEntity<InformeFinalResponseDto> creado = rest.exchange(
                url("/api/informes"), HttpMethod.POST,
                withAuth(informe(), adminToken), InformeFinalResponseDto.class);
        Integer id = creado.getBody().getIdInfFinal();

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/informes/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void psico_crearInforme_devuelve201() {
        // Psicopedagogo tiene info_final.crear
        ResponseEntity<InformeFinalResponseDto> resp = rest.exchange(
                url("/api/informes"), HttpMethod.POST,
                withAuth(informe(), psicoToken), InformeFinalResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void psico_eliminarInforme_devuelve403() {
        // Psicopedagogo no tiene info_final.eliminar
        ResponseEntity<InformeFinalResponseDto> creado = rest.exchange(
                url("/api/informes"), HttpMethod.POST,
                withAuth(informe(), adminToken), InformeFinalResponseDto.class);
        Integer id = creado.getBody().getIdInfFinal();

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/informes/" + id), HttpMethod.DELETE,
                withAuth(psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sinToken_listarInformes_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/informes"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
