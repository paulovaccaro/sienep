package edu.utec.sienep;

import edu.utec.sienep.dto.ITRCreateDto;
import edu.utec.sienep.dto.ITRResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ITRControllerIT extends BaseIT {

    private static final String CI_ADMIN = "50000000";

    private String adminToken;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "ITR", "admin1234", 1, null, null, null);
    }

    @Test
    void admin_listarItrs_devuelveTresDelSeed() {
        ResponseEntity<List<ITRResponseDto>> resp = rest.exchange(
                url("/api/itr"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSizeGreaterThanOrEqualTo(3); // Sur, Norte, Litoral del seed
    }

    @Test
    void admin_obtenerItr_devuelve200() {
        ResponseEntity<ITRResponseDto> resp = rest.exchange(
                url("/api/itr/1"), HttpMethod.GET,
                withAuth(adminToken), ITRResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdItr()).isEqualTo(1);
    }

    @Test
    void admin_crearItr_devuelve201() {
        ITRCreateDto dto = new ITRCreateDto();
        dto.setCodigo("ITR-TEST");
        dto.setNombre("ITR de Prueba");
        dto.setIdDireccion(1); // reutiliza la dirección del seed

        ResponseEntity<ITRResponseDto> resp = rest.exchange(
                url("/api/itr"), HttpMethod.POST,
                withAuth(dto, adminToken), ITRResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getCodigo()).isEqualTo("ITR-TEST");
        assertThat(resp.getBody().getEstActivo()).isTrue();
    }

    @Test
    void admin_actualizarItr_devuelve200() {
        ITRCreateDto crear = new ITRCreateDto();
        crear.setCodigo("ITR-UPD");
        crear.setNombre("ITR Actualizar");
        crear.setIdDireccion(1);
        ResponseEntity<ITRResponseDto> creado = rest.exchange(
                url("/api/itr"), HttpMethod.POST,
                withAuth(crear, adminToken), ITRResponseDto.class);
        Integer id = creado.getBody().getIdItr();

        ITRCreateDto upd = new ITRCreateDto();
        upd.setCodigo("ITR-UPD");
        upd.setNombre("ITR Actualizado");
        upd.setIdDireccion(1);
        ResponseEntity<ITRResponseDto> resp = rest.exchange(
                url("/api/itr/" + id), HttpMethod.PUT,
                withAuth(upd, adminToken), ITRResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getNombre()).isEqualTo("ITR Actualizado");
    }

    @Test
    void admin_desactivarItr_devuelve204() {
        ITRCreateDto crear = new ITRCreateDto();
        crear.setCodigo("ITR-DEL");
        crear.setNombre("ITR Borrar");
        crear.setIdDireccion(1);
        ResponseEntity<ITRResponseDto> creado = rest.exchange(
                url("/api/itr"), HttpMethod.POST,
                withAuth(crear, adminToken), ITRResponseDto.class);
        Integer id = creado.getBody().getIdItr();

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/itr/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void sinToken_listarItrs_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/itr"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
