package edu.utec.sienep;

import edu.utec.sienep.dto.GrupoCreateDto;
import edu.utec.sienep.dto.GrupoResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrupoControllerIT extends BaseIT {

    private static final String CI_ADMIN = "60000008";

    private String adminToken;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "Grupo", "admin1234", 1, null, null, null);
    }

    @Test
    void admin_listarGrupos_devuelveCuatroDelSeed() {
        ResponseEntity<List<GrupoResponseDto>> resp = rest.exchange(
                url("/api/grupos"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    void admin_listarGruposFiltradosPorItr_devuelveGruposDelItr() {
        ResponseEntity<List<GrupoResponseDto>> resp = rest.exchange(
                url("/api/grupos?idItr=1"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
        resp.getBody().forEach(g -> assertThat(g.getIdItr()).isEqualTo(1));
    }

    @Test
    void admin_obtenerGrupo_devuelve200() {
        ResponseEntity<GrupoResponseDto> resp = rest.exchange(
                url("/api/grupos/1"), HttpMethod.GET,
                withAuth(adminToken), GrupoResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdGrupo()).isEqualTo(1);
    }

    @Test
    void admin_crearGrupo_devuelve201() {
        GrupoCreateDto dto = new GrupoCreateDto();
        dto.setNomGrupo("TEST-GRP-2025-1");
        dto.setIdCarrera(1);
        dto.setIdItr(1);
        dto.setAnio(2025);
        dto.setSemestre(1);

        ResponseEntity<GrupoResponseDto> resp = rest.exchange(
                url("/api/grupos"), HttpMethod.POST,
                withAuth(dto, adminToken), GrupoResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getNomGrupo()).isEqualTo("TEST-GRP-2025-1");
        assertThat(resp.getBody().getIdItr()).isEqualTo(1);
    }

    @Test
    void admin_actualizarGrupo_devuelve200() {
        GrupoCreateDto crear = new GrupoCreateDto();
        crear.setNomGrupo("UPD-GRP-2025-1");
        crear.setIdCarrera(1);
        crear.setIdItr(1);
        crear.setAnio(2025);
        crear.setSemestre(2);
        ResponseEntity<GrupoResponseDto> creado = rest.exchange(
                url("/api/grupos"), HttpMethod.POST,
                withAuth(crear, adminToken), GrupoResponseDto.class);
        Integer id = creado.getBody().getIdGrupo();

        GrupoCreateDto upd = new GrupoCreateDto();
        upd.setNomGrupo("UPD-GRP-2025-2");
        upd.setIdCarrera(2);
        upd.setIdItr(1);
        upd.setAnio(2025);
        upd.setSemestre(2);
        ResponseEntity<GrupoResponseDto> resp = rest.exchange(
                url("/api/grupos/" + id), HttpMethod.PUT,
                withAuth(upd, adminToken), GrupoResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getNomGrupo()).isEqualTo("UPD-GRP-2025-2");
    }

    @Test
    void admin_desactivarGrupo_devuelve204() {
        GrupoCreateDto crear = new GrupoCreateDto();
        crear.setNomGrupo("DEL-GRP-2025-1");
        crear.setIdCarrera(1);
        crear.setIdItr(2);
        crear.setAnio(2025);
        crear.setSemestre(1);
        ResponseEntity<GrupoResponseDto> creado = rest.exchange(
                url("/api/grupos"), HttpMethod.POST,
                withAuth(crear, adminToken), GrupoResponseDto.class);
        Integer id = creado.getBody().getIdGrupo();

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/grupos/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void sinToken_listarGrupos_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/grupos"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
