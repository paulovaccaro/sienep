package edu.utec.sienep;

import edu.utec.sienep.dto.InstanciaResponseDto;
import edu.utec.sienep.dto.RecordatorioCreateDto;
import edu.utec.sienep.dto.RecordatorioResponseDto;
import edu.utec.sienep.dto.RecordatorioUpdateDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecordatorioControllerIT extends BaseIT {

    private static final String CI_ADMIN = "20000034";

    private String adminToken;
    private Integer adminId;

    @BeforeAll
    void setup() {
        edu.utec.sienep.dto.LoginResponseDto resp = registrarConId(CI_ADMIN, "Admin", "Rec", "admin1234", 1);
        adminToken = resp.getToken();
        adminId    = resp.getUserId();
    }

    private edu.utec.sienep.dto.LoginResponseDto registrarConId(
            String cedula, String nombre, String apellido, String password, Integer idRol) {
        edu.utec.sienep.dto.RegistroRequestDto req = new edu.utec.sienep.dto.RegistroRequestDto();
        req.setCedula(cedula);
        req.setNombre(nombre);
        req.setApellido(apellido);
        req.setPassword(password);
        req.setFecNacimiento(java.time.LocalDate.of(1985, 1, 1));
        req.setIdRol(idRol);
        ResponseEntity<edu.utec.sienep.dto.LoginResponseDto> resp = rest.postForEntity(
                url("/auth/registro"), req, edu.utec.sienep.dto.LoginResponseDto.class);
        if (!resp.getStatusCode().is2xxSuccessful())
            throw new IllegalStateException("Registro falló: " + cedula);
        return resp.getBody();
    }

    private RecordatorioCreateDto recordatorio(String titulo) {
        RecordatorioCreateDto dto = new RecordatorioCreateDto();
        dto.setTitulo(titulo);
        dto.setDescripcion("Descripción de " + titulo);
        dto.setFecHora(OffsetDateTime.of(2025, 10, 1, 9, 0, 0, 0, ZoneOffset.ofHours(-3)));
        return dto;
    }

    private Integer crearRecordatorio(String titulo) {
        ResponseEntity<RecordatorioResponseDto> resp = rest.exchange(
                url("/api/recordatorios"), HttpMethod.POST,
                withAuth(recordatorio(titulo), adminToken), RecordatorioResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return resp.getBody().getIdRecordatorio();
    }

    @Test
    void admin_crearRecordatorio_devuelve201() {
        ResponseEntity<RecordatorioResponseDto> resp = rest.exchange(
                url("/api/recordatorios"), HttpMethod.POST,
                withAuth(recordatorio("Reunion con tutor"), adminToken), RecordatorioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getTitulo()).isEqualTo("Reunion con tutor");
        assertThat(resp.getBody().getIdFuncionario()).isEqualTo(adminId);
        assertThat(resp.getBody().getEstActivo()).isTrue();
    }

    @Test
    void admin_listarRecordatorios_devuelve200() {
        crearRecordatorio("Recordatorio para listar");

        ResponseEntity<List<RecordatorioResponseDto>> resp = rest.exchange(
                url("/api/recordatorios"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_obtenerRecordatorio_devuelve200() {
        Integer id = crearRecordatorio("Recordatorio para obtener");

        ResponseEntity<RecordatorioResponseDto> resp = rest.exchange(
                url("/api/recordatorios/" + id), HttpMethod.GET,
                withAuth(adminToken), RecordatorioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdRecordatorio()).isEqualTo(id);
    }

    @Test
    void admin_actualizarRecordatorio_devuelve200() {
        Integer id = crearRecordatorio("Recordatorio original");

        RecordatorioUpdateDto upd = new RecordatorioUpdateDto();
        upd.setTitulo("Recordatorio actualizado");
        ResponseEntity<RecordatorioResponseDto> resp = rest.exchange(
                url("/api/recordatorios/" + id), HttpMethod.PUT,
                withAuth(upd, adminToken), RecordatorioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getTitulo()).isEqualTo("Recordatorio actualizado");
    }

    @Test
    void admin_desactivarRecordatorio_devuelve204() {
        Integer id = crearRecordatorio("Recordatorio para borrar");

        ResponseEntity<Void> resp = rest.exchange(
                url("/api/recordatorios/" + id), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void admin_convertirRecordatorioEnInstancia_devuelve201() {
        Integer id = crearRecordatorio("Recordatorio a convertir");

        ResponseEntity<InstanciaResponseDto> resp = rest.exchange(
                url("/api/recordatorios/" + id + "/convertir-instancia"), HttpMethod.POST,
                withAuth(adminToken), InstanciaResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getTitulo()).isEqualTo("Recordatorio a convertir");
    }

    @Test
    void sinToken_listarRecordatorios_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/recordatorios"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
