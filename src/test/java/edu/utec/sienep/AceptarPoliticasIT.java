package edu.utec.sienep;

import edu.utec.sienep.dto.EstudianteCreateDto;
import edu.utec.sienep.dto.EstudianteResponseDto;
import edu.utec.sienep.dto.LoginRequestDto;
import edu.utec.sienep.dto.LoginResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AceptarPoliticasIT extends BaseIT {

    private static final String CI_ADMIN        = "50000044";
    private static final String CI_EST_INACTIVO = "60000042";
    private static final String CI_EST_ACEPTA   = "70000040";
    private static final String CI_EST_YA_ACTIVO = "80000048";
    private static final String PASSWORD        = "pass1234";
    private static final int GRUPO_SUR = 1;

    private String adminToken;
    private String usernameInactivo;
    private String usernameAcepta;
    private String usernameYaActivo;

    @BeforeAll
    void setup() {
        adminToken = registrarYLogin(CI_ADMIN, "Admin", "Pol", "admin1234", 1, null, null, null);

        usernameInactivo  = crearEstudiante(CI_EST_INACTIVO,  "Est", "Inactivo");
        usernameAcepta    = crearEstudiante(CI_EST_ACEPTA,    "Est", "Acepta");
        usernameYaActivo  = crearEstudiante(CI_EST_YA_ACTIVO, "Est", "YaActivo");

        // Activar estYaActivo para poder probar el caso "ya activo → 409"
        LoginRequestDto req = new LoginRequestDto();
        req.setUsername(usernameYaActivo);
        req.setPassword(PASSWORD);
        ResponseEntity<LoginResponseDto> activar = rest.postForEntity(
                url("/auth/aceptar-politicas"), req, LoginResponseDto.class);
        assertThat(activar.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String crearEstudiante(String cedula, String nombre, String apellido) {
        EstudianteCreateDto dto = new EstudianteCreateDto();
        dto.setCedula(cedula);
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        dto.setPassword(PASSWORD);
        dto.setFecNacimiento(LocalDate.of(2000, 6, 15));
        dto.setIdGrupo(GRUPO_SUR);
        ResponseEntity<EstudianteResponseDto> resp = rest.exchange(
                url("/api/estudiantes"), org.springframework.http.HttpMethod.POST,
                withAuth(dto, adminToken), EstudianteResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getEstActivo()).isFalse();
        return resp.getBody().getUsername();
    }

    @Test
    void estudianteInactivo_login_devuelve409() {
        LoginRequestDto req = new LoginRequestDto();
        req.setUsername(usernameInactivo);
        req.setPassword(PASSWORD);

        ResponseEntity<String> resp = rest.postForEntity(url("/auth/login"), req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void estudianteInactivo_aceptarPoliticas_devuelve200YToken() {
        LoginRequestDto req = new LoginRequestDto();
        req.setUsername(usernameAcepta);
        req.setPassword(PASSWORD);

        ResponseEntity<LoginResponseDto> resp = rest.postForEntity(
                url("/auth/aceptar-politicas"), req, LoginResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getToken()).isNotBlank();
        assertThat(resp.getBody().getUsername()).isEqualTo(usernameAcepta);
    }

    @Test
    void estudianteYaActivo_aceptarPoliticas_devuelve409() {
        LoginRequestDto req = new LoginRequestDto();
        req.setUsername(usernameYaActivo);
        req.setPassword(PASSWORD);

        ResponseEntity<String> resp = rest.postForEntity(
                url("/auth/aceptar-politicas"), req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void aceptarPoliticas_passwordIncorrecta_devuelve400() {
        LoginRequestDto req = new LoginRequestDto();
        req.setUsername(usernameInactivo);
        req.setPassword("wrongpass");

        ResponseEntity<String> resp = rest.postForEntity(
                url("/auth/aceptar-politicas"), req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
