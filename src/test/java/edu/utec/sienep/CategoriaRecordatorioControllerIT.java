package edu.utec.sienep;

import edu.utec.sienep.dto.CategoriaRecordatorioCreateDto;
import edu.utec.sienep.dto.CategoriaRecordatorioResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CategoriaRecordatorioControllerIT extends BaseIT {

    private static final String CI_ADMIN = "40000024";

    private String adminToken;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "CatRec", "admin1234", 1, null, null, null);
    }

    private CategoriaRecordatorioCreateDto dto(String nombre) {
        CategoriaRecordatorioCreateDto d = new CategoriaRecordatorioCreateDto();
        d.setNombre(nombre);
        d.setDescripcion("Descripción de " + nombre);
        return d;
    }

    private Integer crearCategoria(String nombre) {
        ResponseEntity<CategoriaRecordatorioResponseDto> resp = rest.exchange(
                url("/api/categorias-recordatorio"), HttpMethod.POST,
                withAuth(dto(nombre), adminToken), CategoriaRecordatorioResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().getIdCategoriaRecordatorio();
    }

    @Test
    void admin_listarCategorias_devuelve200() {
        crearCategoria("CatRec para listar");

        ResponseEntity<List<CategoriaRecordatorioResponseDto>> resp = rest.exchange(
                url("/api/categorias-recordatorio"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_crearCategoria_devuelve201() {
        ResponseEntity<CategoriaRecordatorioResponseDto> resp = rest.exchange(
                url("/api/categorias-recordatorio"), HttpMethod.POST,
                withAuth(dto("Seguimiento mensual"), adminToken), CategoriaRecordatorioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getNombre()).isEqualTo("Seguimiento mensual");
        assertThat(resp.getBody().getEstActivo()).isTrue();
    }

    @Test
    void admin_obtenerCategoria_devuelve200() {
        Integer id = crearCategoria("CatRec para obtener");

        ResponseEntity<CategoriaRecordatorioResponseDto> resp = rest.exchange(
                url("/api/categorias-recordatorio/" + id), HttpMethod.GET,
                withAuth(adminToken), CategoriaRecordatorioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdCategoriaRecordatorio()).isEqualTo(id);
    }

    @Test
    void admin_actualizarCategoria_devuelve200() {
        Integer id = crearCategoria("CatRec original");

        CategoriaRecordatorioCreateDto upd = dto("CatRec actualizada");
        ResponseEntity<CategoriaRecordatorioResponseDto> resp = rest.exchange(
                url("/api/categorias-recordatorio/" + id), HttpMethod.PUT,
                withAuth(upd, adminToken), CategoriaRecordatorioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getNombre()).isEqualTo("CatRec actualizada");
    }

    @Test
    void admin_desactivarCategoria_devuelve204() {
        Integer id = crearCategoria("CatRec para borrar");

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/categorias-recordatorio/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void sinToken_listarCategorias_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/categorias-recordatorio"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
