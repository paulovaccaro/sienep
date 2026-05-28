package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CarreraCreateDto {

    @NotBlank
    private String codigo;

    @NotBlank
    private String nombre;

    @NotBlank
    private String plan;
}
