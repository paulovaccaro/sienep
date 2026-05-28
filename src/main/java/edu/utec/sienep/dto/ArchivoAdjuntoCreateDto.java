package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArchivoAdjuntoCreateDto {

    @NotNull
    private Integer idEstudiante;

    @NotBlank
    private String ruta;

    @NotBlank
    private String categoria;
}
