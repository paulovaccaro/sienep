package edu.utec.sienep;

import edu.utec.sienep.dto.IncidenciaCreateDto;
import edu.utec.sienep.dto.IncidenciaResponseDto;
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

class IncidenciaControllerIT extends BaseIT {

    private static final String CI_ADMIN = "10000020";

    private String adminToken;
    private Integer adminId;
    /** Instancia reutilizada en GET, UPDATE (Incidencia ya creada en setup) */
    private Integer idInstanciaExistente;
    /** Instancia fresca para el test de CREATE */
    private Integer idInstanciaParaCrear;

    @BeforeAll
    void setup() {
        edu.utec.sienep.dto.RegistroRequestDto req = new edu.utec.sienep.dto.RegistroRequestDto();
        req.setCedula(CI_ADMIN);
        req.setNombre("Admin");
        req.setApellido("Inc");
        req.setPassword("admin1234");
        req.setFecNacimiento(LocalDate.of(1985, 1, 1));
        req.setIdRol(1);
        ResponseEntity<LoginResponseDto> loginResp = rest.postForEntity(
                url("/auth/registro"), req, LoginResponseDto.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        adminToken = loginResp.getBody().getToken();
        adminId    = loginResp.getBody().getUserId();

        // Instancia para el test de CREATE (sin Incidencia pre-existente)
        idInstanciaParaCrear = crearInstancia("Instancia para crear incidencia");

        // Instancia con Incidencia ya creada (para GET y UPDATE)
        idInstanciaExistente = crearInstancia("Instancia con incidencia preexistente");
        IncidenciaCreateDto inc = new IncidenciaCreateDto();
        inc.setIdInstancia(idInstanciaExistente);
        inc.setLugar("Aula inicial");
        ResponseEntity<IncidenciaResponseDto> incResp = rest.exchange(
                url("/api/incidencias"), HttpMethod.POST,
                withAuth(inc, adminToken), IncidenciaResponseDto.class);
        assertThat(incResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private Integer crearInstancia(String titulo) {
        InstanciaCreateDto dto = new InstanciaCreateDto();
        dto.setTitulo(titulo);
        dto.setTipo("Incidente");
        dto.setFecHora(OffsetDateTime.of(2025, 9, 1, 9, 0, 0, 0, ZoneOffset.ofHours(-3)));
        dto.setDescripcion("Descripción " + titulo);
        dto.setIdFuncionario(adminId);
        ResponseEntity<InstanciaResponseDto> resp = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(dto, adminToken), InstanciaResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().getIdInstancia();
    }

    @Test
    void admin_crearIncidencia_devuelve201() {
        IncidenciaCreateDto dto = new IncidenciaCreateDto();
        dto.setIdInstancia(idInstanciaParaCrear);
        dto.setLugar("Aula 3");

        ResponseEntity<IncidenciaResponseDto> resp = rest.exchange(
                url("/api/incidencias"), HttpMethod.POST,
                withAuth(dto, adminToken), IncidenciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getLugar()).isEqualTo("Aula 3");
        assertThat(resp.getBody().getIdInstancia()).isEqualTo(idInstanciaParaCrear);
    }

    @Test
    void admin_listarIncidencias_devuelve200() {
        ResponseEntity<List<IncidenciaResponseDto>> resp = rest.exchange(
                url("/api/incidencias"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_obtenerIncidencia_devuelve200() {
        ResponseEntity<IncidenciaResponseDto> resp = rest.exchange(
                url("/api/incidencias/" + idInstanciaExistente), HttpMethod.GET,
                withAuth(adminToken), IncidenciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdInstancia()).isEqualTo(idInstanciaExistente);
    }

    @Test
    void admin_listarIncidenciasPorFuncionario_devuelve200() {
        ResponseEntity<List<IncidenciaResponseDto>> resp = rest.exchange(
                url("/api/incidencias/funcionario/" + adminId), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void admin_actualizarIncidencia_devuelve200() {
        IncidenciaCreateDto upd = new IncidenciaCreateDto();
        upd.setIdInstancia(idInstanciaExistente);
        upd.setLugar("Sala de reuniones");

        ResponseEntity<IncidenciaResponseDto> resp = rest.exchange(
                url("/api/incidencias/" + idInstanciaExistente), HttpMethod.PUT,
                withAuth(upd, adminToken), IncidenciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getLugar()).isEqualTo("Sala de reuniones");
    }

    @Test
    void sinToken_listarIncidencias_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/incidencias"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
