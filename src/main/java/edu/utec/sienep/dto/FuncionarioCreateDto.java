package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FuncionarioCreateDto {

    @NotBlank
    private String cedula;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    private String password;

    @NotNull
    private LocalDate fecNacimiento;
}
