package edu.utec.sienep;

import edu.utec.sienep.dto.InstanciaCreateDto;
import edu.utec.sienep.dto.InstanciaResponseDto;
import edu.utec.sienep.dto.LoginResponseDto;
import edu.utec.sienep.dto.NotificacionCreateDto;
import edu.utec.sienep.dto.NotificacionResponseDto;
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

class NotificacionControllerIT extends BaseIT {

    private static final String CI_ADMIN = "20000028";

    private String adminToken;
    private Integer adminId;
    private Integer idInstancia;

    @BeforeAll
    void setup() {
        edu.utec.sienep.dto.RegistroRequestDto req = new edu.utec.sienep.dto.RegistroRequestDto();
        req.setCedula(CI_ADMIN);
        req.setNombre("Admin");
        req.setApellido("Notif");
        req.setPassword("admin1234");
        req.setFecNacimiento(LocalDate.of(1985, 1, 1));
        req.setIdRol(1);
        ResponseEntity<LoginResponseDto> loginResp = rest.postForEntity(
                url("/auth/registro"), req, LoginResponseDto.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        adminToken = loginResp.getBody().getToken();
        adminId    = loginResp.getBody().getUserId();

        InstanciaCreateDto instDto = new InstanciaCreateDto();
        instDto.setTitulo("Instancia para notificación");
        instDto.setTipo("Reunion");
        instDto.setFecHora(OffsetDateTime.of(2025, 10, 1, 10, 0, 0, 0, ZoneOffset.ofHours(-3)));
        instDto.setDescripcion("Instancia de notificación test");
        instDto.setIdFuncionario(adminId);
        ResponseEntity<InstanciaResponseDto> instResp = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(instDto, adminToken), InstanciaResponseDto.class);
        assertThat(instResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idInstancia = instResp.getBody().getIdInstancia();
    }

    private NotificacionCreateDto notificacion() {
        NotificacionCreateDto dto = new NotificacionCreateDto();
        dto.setIdInstancia(idInstancia);
        dto.setAsunto("Asunto de prueba");
        dto.setMensaje("Mensaje de prueba");
        dto.setDestinatario("destinatario@test.com");
        return dto;
    }

    @Test
    void admin_crearNotificacion_devuelve201() {
        ResponseEntity<NotificacionResponseDto> resp = rest.exchange(
                url("/api/notificaciones"), HttpMethod.POST,
                withAuth(notificacion(), adminToken), NotificacionResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getIdInstancia()).isEqualTo(idInstancia);
        assertThat(resp.getBody().getAsunto()).isEqualTo("Asunto de prueba");
    }

    @Test
    void admin_listarNotificaciones_devuelve200() {
        rest.exchange(url("/api/notificaciones"), HttpMethod.POST,
                withAuth(notificacion(), adminToken), NotificacionResponseDto.class);

        ResponseEntity<List<NotificacionResponseDto>> resp = rest.exchange(
                url("/api/notificaciones"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_obtenerNotificacion_devuelve200() {
        ResponseEntity<NotificacionResponseDto> creada = rest.exchange(
                url("/api/notificaciones"), HttpMethod.POST,
                withAuth(notificacion(), adminToken), NotificacionResponseDto.class);
        Integer id = creada.getBody().getIdNotificacion();

        ResponseEntity<NotificacionResponseDto> resp = rest.exchange(
                url("/api/notificaciones/" + id), HttpMethod.GET,
                withAuth(adminToken), NotificacionResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdNotificacion()).isEqualTo(id);
    }

    @Test
    void admin_listarNotificacionesPorInstancia_devuelve200() {
        rest.exchange(url("/api/notificaciones"), HttpMethod.POST,
                withAuth(notificacion(), adminToken), NotificacionResponseDto.class);

        ResponseEntity<List<NotificacionResponseDto>> resp = rest.exchange(
                url("/api/notificaciones/instancia/" + idInstancia), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
        resp.getBody().forEach(n -> assertThat(n.getIdInstancia()).isEqualTo(idInstancia));
    }

    @Test
    void admin_desactivarNotificacion_devuelve204() {
        ResponseEntity<NotificacionResponseDto> creada = rest.exchange(
                url("/api/notificaciones"), HttpMethod.POST,
                withAuth(notificacion(), adminToken), NotificacionResponseDto.class);
        Integer id = creada.getBody().getIdNotificacion();

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/notificaciones/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void sinToken_listarNotificaciones_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/notificaciones"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
