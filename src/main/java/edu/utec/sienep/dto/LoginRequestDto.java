package edu.utec.sienep.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Credenciales para iniciar sesión")
public class LoginRequestDto {

    @NotBlank
    @Schema(description = "Nombre de usuario (formato nombre.apellido)", example = "juan.perez")
    private String username;

    @NotBlank
    @Schema(description = "Contraseña del usuario (mínimo 8 caracteres)", example = "pass1234")
    private String password;
}
