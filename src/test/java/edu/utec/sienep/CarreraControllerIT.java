package edu.utec.sienep;

import edu.utec.sienep.dto.CarreraCreateDto;
import edu.utec.sienep.dto.CarreraResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CarreraControllerIT extends BaseIT {

    private static final String CI_ADMIN = "40000002";

    private String adminToken;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "Carrera", "admin1234", 1, null, null, null);
    }

    @Test
    void admin_listarCarreras_devuelveListaConSeedDatos() {
        ResponseEntity<List<CarreraResponseDto>> resp = rest.exchange(
                url("/api/carreras"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSizeGreaterThanOrEqualTo(2); // TIC e INFO del seed
    }

    @Test
    void admin_obtenerCarrera_devuelve200() {
        ResponseEntity<CarreraResponseDto> resp = rest.exchange(
                url("/api/carreras/1"), HttpMethod.GET,
                withAuth(adminToken), CarreraResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdCarrera()).isEqualTo(1);
    }

    @Test
    void admin_crearCarrera_devuelve201() {
        CarreraCreateDto dto = new CarreraCreateDto();
        dto.setCodigo("TEST");
        dto.setNombre("Carrera de Test");
        dto.setPlan("2024");

        ResponseEntity<CarreraResponseDto> resp = rest.exchange(
                url("/api/carreras"), HttpMethod.POST,
                withAuth(dto, adminToken), CarreraResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getCodigo()).isEqualTo("TEST");
        assertThat(resp.getBody().getEstActivo()).isTrue();
    }

    @Test
    void admin_actualizarCarrera_devuelve200() {
        // Crear primero
        CarreraCreateDto crear = new CarreraCreateDto();
        crear.setCodigo("UPDT");
        crear.setNombre("Para Actualizar");
        crear.setPlan("2023");
        ResponseEntity<CarreraResponseDto> creada = rest.exchange(
                url("/api/carreras"), HttpMethod.POST,
                withAuth(crear, adminToken), CarreraResponseDto.class);
        Integer id = creada.getBody().getIdCarrera();

        // Actualizar
        CarreraCreateDto upd = new CarreraCreateDto();
        upd.setCodigo("UPDT");
        upd.setNombre("Nombre Actualizado");
        upd.setPlan("2025");
        ResponseEntity<CarreraResponseDto> resp = rest.exchange(
                url("/api/carreras/" + id), HttpMethod.PUT,
                withAuth(upd, adminToken), CarreraResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getNombre()).isEqualTo("Nombre Actualizado");
    }

    @Test
    void admin_desactivarCarrera_devuelve204() {
        CarreraCreateDto crear = new CarreraCreateDto();
        crear.setCodigo("DELT");
        crear.setNombre("Para Borrar");
        crear.setPlan("2022");
        ResponseEntity<CarreraResponseDto> creada = rest.exchange(
                url("/api/carreras"), HttpMethod.POST,
                withAuth(crear, adminToken), CarreraResponseDto.class);
        Integer id = creada.getBody().getIdCarrera();

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/carreras/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void sinToken_listarCarreras_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/carreras"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
