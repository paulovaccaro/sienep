package edu.utec.sienep;

import edu.utec.sienep.dto.*;
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

/**
 * RF18 – Crear instancia desde ficha de alumno.
 * RF15 – Verificación de notificación automática al crear instancia.
 * CIs: 25000001 (admin), 26000002 (estudiante).
 */
class InstanciaFromEstudianteIT extends BaseIT {

    private static final String CI_ADMIN    = "25000001";
    private static final String CI_ANALISTA = "27000003"; // Analista Educativo (idRol=3): solo instancias.leer, sin instancias.crear
    private static final String CI_EST      = "26000002";

    private static final int GRUPO_SUR = 1;
    private static final int ITR_SUR   = 1;

    private String adminToken;
    private Integer adminId;
    private String analistaToken;
    private Integer idEstudiante;

    @BeforeAll
    void setup() {
        LoginResponseDto adminResp = registrarConId(CI_ADMIN, "Admin", "Inst2", "admin1234", 1, null);
        adminToken = adminResp.getToken();
        adminId    = adminResp.getUserId();

        LoginResponseDto analistaResp = registrarConId(CI_ANALISTA, "Analista", "Inst2", "pass1234", 3, null);
        analistaToken = analistaResp.getToken();

        // Crear estudiante (queda inactivo; solo necesitamos su ID para vincular)
        EstudianteCreateDto estDto = new EstudianteCreateDto();
        estDto.setCedula(CI_EST);
        estDto.setNombre("Alumno");
        estDto.setApellido("Ficha");
        estDto.setPassword("pass1234");
        estDto.setFecNacimiento(LocalDate.of(2002, 3, 10));
        estDto.setIdGrupo(GRUPO_SUR);
        ResponseEntity<EstudianteResponseDto> estResp = rest.exchange(
                url("/api/estudiantes"), HttpMethod.POST,
                withAuth(estDto, adminToken), EstudianteResponseDto.class);
        assertThat(estResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idEstudiante = estResp.getBody().getIdUsuario();
    }

    private LoginResponseDto registrarConId(String cedula, String nombre, String apellido,
                                            String password, Integer idRol, Integer idItr) {
        RegistroRequestDto req = new RegistroRequestDto();
        req.setCedula(cedula);
        req.setNombre(nombre);
        req.setApellido(apellido);
        req.setPassword(password);
        req.setFecNacimiento(LocalDate.of(1985, 1, 1));
        req.setIdRol(idRol);
        req.setIdItr(idItr);
        ResponseEntity<LoginResponseDto> resp = rest.postForEntity(url("/auth/registro"), req, LoginResponseDto.class);
        if (!resp.getStatusCode().is2xxSuccessful())
            throw new IllegalStateException("Registro falló: " + cedula);
        return resp.getBody();
    }

    private InstanciaFromEstudianteDto bodyInstancia() {
        InstanciaFromEstudianteDto dto = new InstanciaFromEstudianteDto();
        dto.setTitulo("Reunión desde ficha");
        dto.setTipo("Seguimiento");
        dto.setFecHora(OffsetDateTime.of(2025, 9, 1, 10, 0, 0, 0, ZoneOffset.ofHours(-3)));
        dto.setDescripcion("Instancia creada desde ficha de alumno");
        return dto;
    }

    // ── RF18: creación desde ficha ────────────────────────────────────────

    @Test
    void admin_crearInstanciaDesdeEstudiante_devuelve201() {
        ResponseEntity<InstanciaResponseDto> resp = rest.exchange(
                url("/api/estudiantes/" + idEstudiante + "/instancias"), HttpMethod.POST,
                withAuth(bodyInstancia(), adminToken), InstanciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getTitulo()).isEqualTo("Reunión desde ficha");
        assertThat(resp.getBody().getIdFuncionario()).isEqualTo(adminId);
    }

    @Test
    void admin_crearInstanciaDesdeEstudianteInexistente_devuelve400() {
        ResponseEntity<String> resp = rest.exchange(
                url("/api/estudiantes/999999/instancias"), HttpMethod.POST,
                withAuth(bodyInstancia(), adminToken), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void analista_crearInstanciaDesdeEstudiante_devuelve403() {
        // Analista Educativo tiene instancias.leer pero NO instancias.crear
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/estudiantes/" + idEstudiante + "/instancias"), HttpMethod.POST,
                withAuth(bodyInstancia(), analistaToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void sinToken_crearInstanciaDesdeEstudiante_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/estudiantes/" + idEstudiante + "/instancias"), HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(bodyInstancia()), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── RF15: notificación automática al crear instancia ──────────────────

    @Test
    void admin_crearInstancia_generaNotificacionAutomatica() {
        ResponseEntity<InstanciaResponseDto> creada = rest.exchange(
                url("/api/estudiantes/" + idEstudiante + "/instancias"), HttpMethod.POST,
                withAuth(bodyInstancia(), adminToken), InstanciaResponseDto.class);
        assertThat(creada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Integer idInstancia = creada.getBody().getIdInstancia();

        ResponseEntity<List<NotificacionResponseDto>> notifs = rest.exchange(
                url("/api/notificaciones/instancia/" + idInstancia), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(notifs.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(notifs.getBody()).isNotEmpty();
        assertThat(notifs.getBody().get(0).getAsunto()).contains(String.valueOf(idInstancia));
    }

    @Test
    void admin_crearInstanciaDirecta_tambienGeneraNotificacion() {
        InstanciaCreateDto dto = new InstanciaCreateDto();
        dto.setTitulo("Instancia directa");
        dto.setTipo("Reunion");
        dto.setFecHora(OffsetDateTime.of(2025, 10, 1, 9, 0, 0, 0, ZoneOffset.ofHours(-3)));
        dto.setDescripcion("Test RF15 instancia directa");
        dto.setIdFuncionario(adminId);

        ResponseEntity<InstanciaResponseDto> creada = rest.exchange(
                url("/api/instancias"), HttpMethod.POST,
                withAuth(dto, adminToken), InstanciaResponseDto.class);
        assertThat(creada.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Integer idInstancia = creada.getBody().getIdInstancia();

        ResponseEntity<List<NotificacionResponseDto>> notifs = rest.exchange(
                url("/api/notificaciones/instancia/" + idInstancia), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(notifs.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(notifs.getBody()).isNotEmpty();
    }
}
