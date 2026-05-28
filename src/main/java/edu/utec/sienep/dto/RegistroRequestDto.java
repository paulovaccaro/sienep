package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegistroRequestDto {

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

    /** 1=Administrador, 2=Psicopedagogo, 3=Analista Educativo, etc. Default: 1 */
    private Integer idRol = 1;

    private Integer idItr;
    private Integer idCarrera;
    private Integer idGrupo;
}
