package edu.utec.sienep.controller;

import edu.utec.sienep.dto.CambiarPasswordDto;
import edu.utec.sienep.dto.LoginRequestDto;
import edu.utec.sienep.dto.LoginResponseDto;
import edu.utec.sienep.dto.RegistroRequestDto;
import edu.utec.sienep.entity.Usuario;
import edu.utec.sienep.security.JwtUtil;
import edu.utec.sienep.service.LoginService;
import edu.utec.sienep.service.RegistroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login, registro, aceptación de políticas, cierre de sesión y cambio de contraseña")
public class AuthController {

    private final LoginService loginService;
    private final RegistroService registroService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Iniciar sesión (RF01/RF02)", description = "Devuelve un JWT para el usuario autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso — retorna token JWT"),
        @ApiResponse(responseCode = "400", description = "Credenciales inválidas o usuario inactivo")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        Usuario usuario = loginService.autenticar(request.getUsername(), request.getPassword());
        String token = jwtUtil.generarToken(usuario.getIdUsuario(), usuario.getUsername());
        return ResponseEntity.ok(new LoginResponseDto(token, usuario.getIdUsuario(), usuario.getUsername()));
    }

    @Operation(summary = "Registrar funcionario", description = "Crea un funcionario; idItr/idCarrera/idGrupo opcionales para scope inicial")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Funcionario creado — retorna token JWT"),
        @ApiResponse(responseCode = "400", description = "CI inválida, menor de edad, password corta o CI duplicada")
    })
    @PostMapping("/registro")
    public ResponseEntity<LoginResponseDto> registro(@Valid @RequestBody RegistroRequestDto request) {
        Usuario usuario = registroService.registrarFuncionario(
                request.getCedula(), request.getNombre(), request.getApellido(),
                request.getPassword(), request.getFecNacimiento(), request.getIdRol(),
                request.getIdItr(), request.getIdCarrera(), request.getIdGrupo());
        String token = jwtUtil.generarToken(usuario.getIdUsuario(), usuario.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponseDto(token, usuario.getIdUsuario(), usuario.getUsername()));
    }

    @Operation(summary = "Aceptar políticas de uso", description = "Activa un usuario inactivo y devuelve un JWT")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Políticas aceptadas — usuario activado, retorna token JWT"),
        @ApiResponse(responseCode = "400", description = "Credenciales inválidas o usuario ya activo")
    })
    @PostMapping("/aceptar-politicas")
    public ResponseEntity<LoginResponseDto> aceptarPoliticas(@Valid @RequestBody LoginRequestDto request) {
        Usuario usuario = loginService.aceptarPoliticas(request.getUsername(), request.getPassword());
        String token = jwtUtil.generarToken(usuario.getIdUsuario(), usuario.getUsername());
        return ResponseEntity.ok(new LoginResponseDto(token, usuario.getIdUsuario(), usuario.getUsername()));
    }

    @Operation(summary = "Cerrar sesión (RF04)", description = "JWT es stateless: el cliente descarta el token. Este endpoint confirma el cierre y registra la auditoría")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Sesión cerrada correctamente"),
        @ApiResponse(responseCode = "401", description = "Token no proporcionado o inválido")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(Authentication auth) {
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada. Descarte el token en el cliente."));
    }

    @Operation(summary = "Cambiar contraseña (RF03)", description = "El usuario autenticado cambia su propia contraseña")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contraseña actualizada correctamente"),
        @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o nueva contraseña muy corta"),
        @ApiResponse(responseCode = "401", description = "Token no proporcionado o inválido")
    })
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> cambiarPassword(
            @Valid @RequestBody CambiarPasswordDto dto,
            Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        loginService.cambiarPassword(userId, dto.getPasswordActual(), dto.getPasswordNueva());
        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente"));
    }
}
