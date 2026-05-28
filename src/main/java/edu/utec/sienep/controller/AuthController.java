package edu.utec.sienep.controller;

import edu.utec.sienep.dto.LoginRequestDto;
import edu.utec.sienep.dto.LoginResponseDto;
import edu.utec.sienep.dto.RegistroRequestDto;
import edu.utec.sienep.entity.Usuario;
import edu.utec.sienep.security.JwtUtil;
import edu.utec.sienep.service.LoginService;
import edu.utec.sienep.service.RegistroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login, registro y aceptación de políticas")
public class AuthController {

    private final LoginService loginService;
    private final RegistroService registroService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "Iniciar sesión", description = "Devuelve un JWT para el usuario autenticado")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        Usuario usuario = loginService.autenticar(request.getUsername(), request.getPassword());
        String token = jwtUtil.generarToken(usuario.getIdUsuario(), usuario.getUsername());
        return ResponseEntity.ok(new LoginResponseDto(token, usuario.getIdUsuario(), usuario.getUsername()));
    }

    @Operation(summary = "Registrar funcionario", description = "Crea un funcionario; idItr/idCarrera/idGrupo opcionales para scope inicial")
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
    @PostMapping("/aceptar-politicas")
    public ResponseEntity<LoginResponseDto> aceptarPoliticas(@Valid @RequestBody LoginRequestDto request) {
        Usuario usuario = loginService.aceptarPoliticas(request.getUsername(), request.getPassword());
        String token = jwtUtil.generarToken(usuario.getIdUsuario(), usuario.getUsername());
        return ResponseEntity.ok(new LoginResponseDto(token, usuario.getIdUsuario(), usuario.getUsername()));
    }
}
