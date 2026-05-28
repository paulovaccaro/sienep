package edu.utec.sienep;

import edu.utec.sienep.dto.LoginRequestDto;
import edu.utec.sienep.dto.LoginResponseDto;
import edu.utec.sienep.dto.RegistroRequestDto;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseIT {

    @Value("${local.server.port}")
    protected int port;

    protected final RestTemplate rest;

    protected BaseIT() {
        rest = new RestTemplate();
        // Suppress exceptions on 4xx/5xx so tests can assert on status codes
        rest.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
        });
    }

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    protected String registrarYLogin(String cedula, String nombre, String apellido,
                                     String password, Integer idRol,
                                     Integer idItr, Integer idCarrera, Integer idGrupo) {
        RegistroRequestDto req = new RegistroRequestDto();
        req.setCedula(cedula);
        req.setNombre(nombre);
        req.setApellido(apellido);
        req.setPassword(password);
        req.setFecNacimiento(LocalDate.of(1990, 1, 1));
        req.setIdRol(idRol);
        req.setIdItr(idItr);
        req.setIdCarrera(idCarrera);
        req.setIdGrupo(idGrupo);

        ResponseEntity<LoginResponseDto> resp = rest.postForEntity(
                url("/auth/registro"), req, LoginResponseDto.class);
        if (!resp.getStatusCode().is2xxSuccessful())
            throw new IllegalStateException("Registro falló [" + resp.getStatusCode() + "]: " + cedula);
        return resp.getBody().getToken();
    }

    protected String login(String username, String password) {
        LoginRequestDto req = new LoginRequestDto();
        req.setUsername(username);
        req.setPassword(password);
        ResponseEntity<LoginResponseDto> resp = rest.postForEntity(
                url("/auth/login"), req, LoginResponseDto.class);
        if (!resp.getStatusCode().is2xxSuccessful())
            throw new IllegalStateException("Login falló para " + username);
        return resp.getBody().getToken();
    }

    protected HttpHeaders bearer(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    protected <T> HttpEntity<T> withAuth(T body, String token) {
        return new HttpEntity<>(body, bearer(token));
    }

    protected HttpEntity<Void> withAuth(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return new HttpEntity<>(h);
    }
}
