package edu.utec.sienep.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Respuesta de autenticación con JWT")
public class LoginResponseDto {

    @Schema(description = "Token JWT para usar en el header Authorization: Bearer {token}")
    private String token;

    @Schema(description = "ID del usuario autenticado", example = "1")
    private Integer userId;

    @Schema(description = "Nombre de usuario", example = "juan.perez")
    private String username;
}
