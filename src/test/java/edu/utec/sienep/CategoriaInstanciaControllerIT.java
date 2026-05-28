package edu.utec.sienep;

import edu.utec.sienep.dto.CategoriaInstanciaCreateDto;
import edu.utec.sienep.dto.CategoriaInstanciaResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoriaInstanciaControllerIT extends BaseIT {

    private static final String CI_ADMIN = "30000026";

    private String adminToken;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "CatInst", "admin1234", 1, null, null, null);
    }

    private CategoriaInstanciaCreateDto dto(String nombre) {
        CategoriaInstanciaCreateDto d = new CategoriaInstanciaCreateDto();
        d.setNombre(nombre);
        d.setDescripcion("Descripción de " + nombre);
        return d;
    }

    private Integer crearCategoria(String nombre) {
        ResponseEntity<CategoriaInstanciaResponseDto> resp = rest.exchange(
                url("/api/categorias-instancia"), HttpMethod.POST,
                withAuth(dto(nombre), adminToken), CategoriaInstanciaResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().getIdCategoriaInstancia();
    }

    @Test
    void admin_listarCategorias_devuelve200() {
        crearCategoria("Categoria para listar");

        ResponseEntity<List<CategoriaInstanciaResponseDto>> resp = rest.exchange(
                url("/api/categorias-instancia"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_crearCategoria_devuelve201() {
        ResponseEntity<CategoriaInstanciaResponseDto> resp = rest.exchange(
                url("/api/categorias-instancia"), HttpMethod.POST,
                withAuth(dto("Reunion semanal"), adminToken), CategoriaInstanciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getNombre()).isEqualTo("Reunion semanal");
        assertThat(resp.getBody().getEstActivo()).isTrue();
    }

    @Test
    void admin_obtenerCategoria_devuelve200() {
        Integer id = crearCategoria("Cat para obtener");

        ResponseEntity<CategoriaInstanciaResponseDto> resp = rest.exchange(
                url("/api/categorias-instancia/" + id), HttpMethod.GET,
                withAuth(adminToken), CategoriaInstanciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdCategoriaInstancia()).isEqualTo(id);
    }

    @Test
    void admin_actualizarCategoria_devuelve200() {
        Integer id = crearCategoria("Cat original");

        CategoriaInstanciaCreateDto upd = dto("Cat actualizada");
        ResponseEntity<CategoriaInstanciaResponseDto> resp = rest.exchange(
                url("/api/categorias-instancia/" + id), HttpMethod.PUT,
                withAuth(upd, adminToken), CategoriaInstanciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getNombre()).isEqualTo("Cat actualizada");
    }

    @Test
    void admin_desactivarCategoria_devuelve204() {
        Integer id = crearCategoria("Cat para borrar");

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/categorias-instancia/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void sinToken_listarCategorias_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/categorias-instancia"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
