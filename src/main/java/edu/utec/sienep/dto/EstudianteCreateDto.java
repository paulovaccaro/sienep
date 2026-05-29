package edu.utec.sienep.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Datos para dar de alta un estudiante (RF05)")
public class EstudianteCreateDto {

    @NotBlank
    @Schema(description = "Cédula de identidad uruguaya válida (8 dígitos)", example = "45678901")
    private String cedula;

    @NotBlank
    @Schema(description = "Nombre del estudiante", example = "María")
    private String nombre;

    @NotBlank
    @Schema(description = "Apellido del estudiante", example = "Fernández")
    private String apellido;

    @NotBlank
    @Schema(description = "Contraseña inicial (mínimo 8 caracteres)", example = "pass1234")
    private String password;

    @NotNull
    @Schema(description = "Fecha de nacimiento — debe ser mayor de 18 años", example = "2000-03-20")
    private LocalDate fecNacimiento;

    @NotNull
    @Schema(description = "ID del grupo académico al que pertenece el estudiante", example = "1")
    private Integer idGrupo;
}
