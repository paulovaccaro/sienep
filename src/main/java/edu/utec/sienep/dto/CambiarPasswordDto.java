package edu.utec.sienep.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Datos para cambio de contraseña (RF03)")
public class CambiarPasswordDto {

    @NotBlank
    @Schema(description = "Contraseña actual del usuario", example = "pass1234")
    private String passwordActual;

    @NotBlank
    @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
    @Schema(description = "Nueva contraseña (mínimo 8 caracteres)", example = "nuevaPass9999")
    private String passwordNueva;
}
