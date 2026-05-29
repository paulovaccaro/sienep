package edu.utec.sienep.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Datos para registrar un funcionario en el sistema")
public class RegistroRequestDto {

    @NotBlank
    @Schema(description = "Cédula de identidad uruguaya (8 dígitos con dígito verificador)", example = "12345672")
    private String cedula;

    @NotBlank
    @Schema(description = "Nombre del funcionario", example = "Juan")
    private String nombre;

    @NotBlank
    @Schema(description = "Apellido del funcionario", example = "Perez")
    private String apellido;

    @NotBlank
    @Schema(description = "Contraseña (mínimo 8 caracteres)", example = "pass1234")
    private String password;

    @NotNull
    @Schema(description = "Fecha de nacimiento — debe ser mayor de 18 años", example = "1990-05-15")
    private LocalDate fecNacimiento;

    @Schema(description = "ID del rol: 1=Administrador, 2=Psicopedagogo, 3=Analista Educativo, 4=Responsable Educativo", example = "1")
    private Integer idRol = 1;

    @Schema(description = "ID del ITR para scope inicial (opcional)", example = "1")
    private Integer idItr;

    @Schema(description = "ID de la carrera para scope inicial (opcional)", example = "1")
    private Integer idCarrera;

    @Schema(description = "ID del grupo para scope inicial (opcional)", example = "1")
    private Integer idGrupo;
}
