package edu.utec.sienep;

import edu.utec.sienep.dto.LoginRequestDto;
import edu.utec.sienep.dto.LoginResponseDto;
import edu.utec.sienep.dto.RegistroRequestDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerIT extends BaseIT {

    private static final String CI_ADMIN = "11111111";
    private static final String CI_NUEVO = "22222222";

    private String adminUsername;

    @BeforeAll
    void setup() {
        RegistroRequestDto req = new RegistroRequestDto();
        req.setCedula(CI_ADMIN);
        req.setNombre("Admin");
        req.setApellido("Test");
        req.setPassword("admin1234");
        req.setFecNacimiento(LocalDate.of(1985, 6, 15));
        req.setIdRol(1);

        ResponseEntity<LoginResponseDto> resp = rest.postForEntity(
                url("/auth/registro"), req, LoginResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getToken()).isNotBlank();
        adminUsername = resp.getBody().getUsername();
    }

    @Test
    void registro_devuelveTokenYCreado() {
        RegistroRequestDto req = new RegistroRequestDto();
        req.setCedula(CI_NUEVO);
        req.setNombre("Nuevo");
        req.setApellido("Usuario");
        req.setPassword("password123");
        req.setFecNacimiento(LocalDate.of(1992, 3, 20));
        req.setIdRol(1);

        ResponseEntity<LoginResponseDto> resp = rest.postForEntity(
                url("/auth/registro"), req, LoginResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getToken()).isNotBlank();
        assertThat(resp.getBody().getUsername()).isEqualTo("nuevo.usuario");
    }

    @Test
    void registro_ciInvalida_devuelve400() {
        RegistroRequestDto req = new RegistroRequestDto();
        req.setCedula("12345678"); // dígito verificador incorrecto
        req.setNombre("Mal");
        req.setApellido("CI");
        req.setPassword("password123");
        req.setFecNacimiento(LocalDate.of(1990, 1, 1));
        req.setIdRol(1);

        ResponseEntity<String> resp = rest.postForEntity(
                url("/auth/registro"), req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void registro_passwordCorta_devuelve400() {
        RegistroRequestDto req = new RegistroRequestDto();
        req.setCedula("33333333");
        req.setNombre("Pass");
        req.setApellido("Corta");
        req.setPassword("abc");
        req.setFecNacimiento(LocalDate.of(1990, 1, 1));
        req.setIdRol(1);

        ResponseEntity<String> resp = rest.postForEntity(
                url("/auth/registro"), req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void registro_menorEdad_devuelve400() {
        RegistroRequestDto req = new RegistroRequestDto();
        req.setCedula("44444444");
        req.setNombre("Menor");
        req.setApellido("Edad");
        req.setPassword("password123");
        req.setFecNacimiento(LocalDate.now().minusYears(16));
        req.setIdRol(1);

        ResponseEntity<String> resp = rest.postForEntity(
                url("/auth/registro"), req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_credencialesCorrectas_devuelveToken() {
        String token = login(adminUsername, "admin1234");
        assertThat(token).isNotBlank();
    }

    @Test
    void login_passwordIncorrecta_devuelve400() {
        LoginRequestDto req = new LoginRequestDto();
        req.setUsername(adminUsername);
        req.setPassword("wrongpassword");

        ResponseEntity<String> resp = rest.postForEntity(
                url("/auth/login"), req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_usuarioInexistente_devuelve400() {
        LoginRequestDto req = new LoginRequestDto();
        req.setUsername("no.existe");
        req.setPassword("password123");

        ResponseEntity<String> resp = rest.postForEntity(
                url("/auth/login"), req, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
