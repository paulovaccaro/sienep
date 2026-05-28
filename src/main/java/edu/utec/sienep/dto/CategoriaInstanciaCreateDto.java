package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoriaInstanciaCreateDto {

    @NotBlank
    private String nombre;

    private String descripcion;
}
