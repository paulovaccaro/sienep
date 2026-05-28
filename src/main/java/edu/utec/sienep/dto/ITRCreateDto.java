package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ITRCreateDto {

    @NotBlank
    private String codigo;

    @NotBlank
    private String nombre;

    @NotNull
    private Integer idDireccion;
}
