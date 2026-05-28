package edu.utec.sienep;

import edu.utec.sienep.dto.EventoCalendarioCreateDto;
import edu.utec.sienep.dto.EventoCalendarioResponseDto;
import edu.utec.sienep.dto.InstanciaCreateDto;
import edu.utec.sienep.dto.InstanciaResponseDto;
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

class EventoCalendarioControllerIT extends BaseIT {

    private static final String CI_ADMIN = "50000022";

    private String adminToken;
    private Integer adminId;
    private Integer idInstancia;

    @BeforeAll
    void setup() {
        edu.utec.sienep.dto.RegistroRequestDto req = new edu.utec.sienep.dto.RegistroRequestDto();
        req.setCedula(CI_ADMIN);
        req.setNombre("Admin");
        req.setApellido("Evt");
        req.setPassword("admin1234");
        req.setFecNacimiento(LocalDate.of(1985, 1, 1));
        req.setIdRol(1);
        ResponseEntity<edu.utec.sienep.dto.LoginResponseDto> loginResp = rest.postForEntity(
                url("/auth/registro"), req, edu.utec.sienep.dto.LoginResponseDto.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        adminToken = loginResp.getBody().getToken();
        adminId    = loginResp.getBody().getUserId();

        // Crear instancia → genera EventoCalendario automáticamente
        InstanciaCreateDto instDto = new InstanciaCreateDto();
        instDto.setTitulo("Instancia para eventos");
        instDto.setTipo("Reunion");
        instDto.setFecHora(OffsetDateTime.of(2025, 10, 10, 10, 0, 0, 0, ZoneOffset.ofHours(-3)));
        instDto.setDescripcion("Desc");
        instDto.setIdFuncionario(adminId);
        ResponseEntity<InstanciaResponseDto> instResp = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(instDto, adminToken), InstanciaResponseDto.class);
        assertThat(instResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idInstancia = instResp.getBody().getIdInstancia();
    }

    private EventoCalendarioCreateDto eventoDto(String titulo) {
        EventoCalendarioCreateDto d = new EventoCalendarioCreateDto();
        d.setTitulo(titulo);
        d.setDescripcion("Descripción del evento");
        d.setFecInicio(OffsetDateTime.of(2025, 11, 1, 9, 0, 0, 0, ZoneOffset.ofHours(-3)));
        d.setFecFin(OffsetDateTime.of(2025, 11, 1, 10, 0, 0, 0, ZoneOffset.ofHours(-3)));
        return d;
    }

    private Integer crearEvento(String titulo) {
        ResponseEntity<EventoCalendarioResponseDto> resp = rest.exchange(
                url("/api/eventos-calendario"), HttpMethod.POST,
                withAuth(eventoDto(titulo), adminToken), EventoCalendarioResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().getIdEventoCalendario();
    }

    @Test
    void admin_listarEventos_devuelve200() {
        ResponseEntity<List<EventoCalendarioResponseDto>> resp = rest.exchange(
                url("/api/eventos-calendario"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_listarEventosPendientes_devuelve200() {
        ResponseEntity<List<EventoCalendarioResponseDto>> resp = rest.exchange(
                url("/api/eventos-calendario/pendientes"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void admin_listarEventosPorInstancia_devuelve200() {
        ResponseEntity<List<EventoCalendarioResponseDto>> resp = rest.exchange(
                url("/api/eventos-calendario/instancia/" + idInstancia), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_crearEvento_devuelve201() {
        ResponseEntity<EventoCalendarioResponseDto> resp = rest.exchange(
                url("/api/eventos-calendario"), HttpMethod.POST,
                withAuth(eventoDto("Evento manual"), adminToken), EventoCalendarioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getTitulo()).isEqualTo("Evento manual");
        assertThat(resp.getBody().getEstActivo()).isTrue();
    }

    @Test
    void admin_obtenerEvento_devuelve200() {
        Integer id = crearEvento("Evento para obtener");

        ResponseEntity<EventoCalendarioResponseDto> resp = rest.exchange(
                url("/api/eventos-calendario/" + id), HttpMethod.GET,
                withAuth(adminToken), EventoCalendarioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdEventoCalendario()).isEqualTo(id);
    }

    @Test
    void admin_actualizarEvento_devuelve200() {
        Integer id = crearEvento("Evento original");

        EventoCalendarioCreateDto upd = eventoDto("Evento actualizado");
        ResponseEntity<EventoCalendarioResponseDto> resp = rest.exchange(
                url("/api/eventos-calendario/" + id), HttpMethod.PUT,
                withAuth(upd, adminToken), EventoCalendarioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getTitulo()).isEqualTo("Evento actualizado");
    }

    @Test
    void admin_sincronizarEvento_devuelve200() {
        Integer id = crearEvento("Evento a sincronizar");

        ResponseEntity<EventoCalendarioResponseDto> resp = rest.exchange(
                url("/api/eventos-calendario/" + id + "/sincronizar"), HttpMethod.POST,
                withAuth(adminToken), EventoCalendarioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getSincronizado()).isTrue();
    }

    @Test
    void admin_desactivarEvento_devuelve204() {
        Integer id = crearEvento("Evento para borrar");

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/eventos-calendario/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void sinToken_listarEventos_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/eventos-calendario"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
