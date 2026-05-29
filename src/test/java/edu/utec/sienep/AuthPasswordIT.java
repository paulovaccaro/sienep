package edu.utec.sienep;

import edu.utec.sienep.dto.CambiarPasswordDto;
import edu.utec.sienep.dto.LoginRequestDto;
import edu.utec.sienep.dto.LoginResponseDto;
import edu.utec.sienep.dto.RegistroRequestDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RF03 – Cambio de contraseña / RF04 – Cierre de sesión.
 * CI: 17000005 (usuario de prueba).
 */
class AuthPasswordIT extends BaseIT {

    private static final String CI_USER  = "17000005";
    private static final String PASSWORD = "pass1234";

    private String token;
    private String username;

    @BeforeAll
    void setup() {
        RegistroRequestDto req = new RegistroRequestDto();
        req.setCedula(CI_USER);
        req.setNombre("Pass");
        req.setApellido("Change");
        req.setPassword(PASSWORD);
        req.setFecNacimiento(LocalDate.of(1990, 5, 20));
        req.setIdRol(1);

        ResponseEntity<LoginResponseDto> resp = rest.postForEntity(
                url("/auth/registro"), req, LoginResponseDto.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        token    = resp.getBody().getToken();
        username = resp.getBody().getUsername();
    }

    // ── logout (RF04) ──────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void logout_conToken_devuelve200ConMensaje() {
        ResponseEntity<Map> resp = rest.exchange(
                url("/auth/logout"),
                org.springframework.http.HttpMethod.POST,
                withAuth(token), Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsKey("mensaje");
    }

    @Test
    void logout_sinToken_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/auth/logout"),
                org.springframework.http.HttpMethod.POST,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ── cambio de contraseña (RF03) ────────────────────────────────────────

    @Test
    void cambiarPassword_correcta_devuelve200() {
        CambiarPasswordDto dto = new CambiarPasswordDto();
        dto.setPasswordActual(PASSWORD);
        dto.setPasswordNueva("nuevaPass9999");

        ResponseEntity<Map> resp = rest.exchange(
                url("/auth/password"),
                org.springframework.http.HttpMethod.PUT,
                withAuth(dto, token), Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsKey("mensaje");

        // Revertir para no romper otros tests que usen el mismo usuario
        CambiarPasswordDto revert = new CambiarPasswordDto();
        revert.setPasswordActual("nuevaPass9999");
        revert.setPasswordNueva(PASSWORD);
        rest.exchange(url("/auth/password"),
                org.springframework.http.HttpMethod.PUT,
                withAuth(dto, token), Map.class);
    }

    @Test
    void cambiarPassword_passwordActualIncorrecta_devuelve400() {
        CambiarPasswordDto dto = new CambiarPasswordDto();
        dto.setPasswordActual("wrongPassword");
        dto.setPasswordNueva("nuevaPass9999");

        ResponseEntity<String> resp = rest.exchange(
                url("/auth/password"),
                org.springframework.http.HttpMethod.PUT,
                withAuth(dto, token), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void cambiarPassword_nuevaPasswordCorta_devuelve400() {
        CambiarPasswordDto dto = new CambiarPasswordDto();
        dto.setPasswordActual(PASSWORD);
        dto.setPasswordNueva("abc");

        ResponseEntity<String> resp = rest.exchange(
                url("/auth/password"),
                org.springframework.http.HttpMethod.PUT,
                withAuth(dto, token), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void cambiarPassword_sinToken_devuelve401() {
        CambiarPasswordDto dto = new CambiarPasswordDto();
        dto.setPasswordActual(PASSWORD);
        dto.setPasswordNueva("nuevaPass9999");

        ResponseEntity<Void> resp = rest.exchange(
                url("/auth/password"),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(dto), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void cambiarPassword_NuevoLoginConPasswordCambiada_funciona() {
        String nuevaPass = "cambiada5678";

        CambiarPasswordDto dto = new CambiarPasswordDto();
        dto.setPasswordActual(PASSWORD);
        dto.setPasswordNueva(nuevaPass);
        ResponseEntity<Map> cambio = rest.exchange(
                url("/auth/password"),
                org.springframework.http.HttpMethod.PUT,
                withAuth(dto, token), Map.class);
        assertThat(cambio.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Login con la nueva contraseña debe funcionar
        LoginRequestDto loginReq = new LoginRequestDto();
        loginReq.setUsername(username);
        loginReq.setPassword(nuevaPass);
        ResponseEntity<LoginResponseDto> loginResp = rest.postForEntity(
                url("/auth/login"), loginReq, LoginResponseDto.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResp.getBody().getToken()).isNotBlank();

        // Restaurar contraseña original
        CambiarPasswordDto revert = new CambiarPasswordDto();
        revert.setPasswordActual(nuevaPass);
        revert.setPasswordNueva(PASSWORD);
        String tokenNuevo = loginResp.getBody().getToken();
        rest.exchange(url("/auth/password"),
                org.springframework.http.HttpMethod.PUT,
                withAuth(revert, tokenNuevo), Map.class);
    }
}
