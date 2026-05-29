package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RolCreateDto {

    @NotBlank
    private String nombre;

    private String descripcion;
}
