package edu.utec.sienep;

import edu.utec.sienep.dto.FuncionarioCreateDto;
import edu.utec.sienep.dto.FuncionarioResponseDto;
import edu.utec.sienep.dto.LoginResponseDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FuncionarioControllerIT extends BaseIT {

    // CIs válidas únicas para esta suite
    private static final String CI_ADMIN      = "10000008";
    private static final String CI_PSICO      = "20000006";
    private static final String CI_NUEVO      = "30000004";
    private static final String CI_PARA_BORRAR = "10010001"; // check = (10-(2+7))%10 = 1

    private String adminToken;
    private Integer adminId;
    private String psicoToken;
    private Integer psicoId;
    private Integer idParaBorrar;

    @BeforeAll
    void setup() {
        LoginResponseDto adminResp = registrarYLoginConId(CI_ADMIN, "Admin", "Func", "admin1234", 1, null, null, null);
        adminToken = adminResp.getToken();
        adminId    = adminResp.getUserId();

        LoginResponseDto psicoResp = registrarYLoginConId(CI_PSICO, "Psico", "Func", "pass1234", 2, 1, null, null);
        psicoToken = psicoResp.getToken();
        psicoId    = psicoResp.getUserId();

        LoginResponseDto borrarResp = registrarYLoginConId(CI_PARA_BORRAR, "Para", "Borrar", "pass1234", 3, null, null, null);
        idParaBorrar = borrarResp.getUserId();
    }

    // helper que devuelve el DTO completo en vez de solo el token
    private LoginResponseDto registrarYLoginConId(String cedula, String nombre, String apellido,
                                                  String password, Integer idRol,
                                                  Integer idItr, Integer idCarrera, Integer idGrupo) {
        edu.utec.sienep.dto.RegistroRequestDto req = new edu.utec.sienep.dto.RegistroRequestDto();
        req.setCedula(cedula);
        req.setNombre(nombre);
        req.setApellido(apellido);
        req.setPassword(password);
        req.setFecNacimiento(LocalDate.of(1985, 1, 1));
        req.setIdRol(idRol);
        req.setIdItr(idItr);
        req.setIdCarrera(idCarrera);
        req.setIdGrupo(idGrupo);
        ResponseEntity<LoginResponseDto> resp = rest.postForEntity(url("/auth/registro"), req, LoginResponseDto.class);
        if (!resp.getStatusCode().is2xxSuccessful())
            throw new IllegalStateException("Registro falló: " + cedula);
        return resp.getBody();
    }

    @Test
    void admin_listarFuncionarios_devuelve200() {
        ResponseEntity<List<FuncionarioResponseDto>> resp = rest.exchange(
                url("/api/funcionarios"), HttpMethod.GET,
                withAuth(adminToken), new ParameterizedTypeReference<>() {});

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotEmpty();
    }

    @Test
    void admin_obtenerFuncionario_devuelve200() {
        ResponseEntity<FuncionarioResponseDto> resp = rest.exchange(
                url("/api/funcionarios/" + adminId), HttpMethod.GET,
                withAuth(adminToken), FuncionarioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdUsuario()).isEqualTo(adminId);
    }

    @Test
    void admin_crearFuncionario_devuelve201() {
        FuncionarioCreateDto dto = new FuncionarioCreateDto();
        dto.setCedula(CI_NUEVO);
        dto.setNombre("Nuevo");
        dto.setApellido("Funcionario");
        dto.setPassword("pass1234");
        dto.setFecNacimiento(LocalDate.of(1990, 6, 15));

        ResponseEntity<FuncionarioResponseDto> resp = rest.exchange(
                url("/api/funcionarios"), HttpMethod.POST,
                withAuth(dto, adminToken), FuncionarioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody().getCedula()).isEqualTo(CI_NUEVO);
    }

    @Test
    void admin_desactivarFuncionario_devuelve204() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/funcionarios/" + idParaBorrar), HttpMethod.DELETE,
                withAuth(adminToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void psico_listarFuncionarios_devuelve403() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/funcionarios"), HttpMethod.GET,
                withAuth(psicoToken), Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void psico_obtenerPropioFuncionario_devuelve200() {
        ResponseEntity<FuncionarioResponseDto> resp = rest.exchange(
                url("/api/funcionarios/" + psicoId), HttpMethod.GET,
                withAuth(psicoToken), FuncionarioResponseDto.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getIdUsuario()).isEqualTo(psicoId);
    }

    @Test
    void sinToken_listarFuncionarios_devuelve401() {
        ResponseEntity<Void> resp = rest.exchange(
                url("/api/funcionarios"), HttpMethod.GET,
                null, Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
