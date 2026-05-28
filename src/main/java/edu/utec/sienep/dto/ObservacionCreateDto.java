package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ObservacionCreateDto {

    @NotNull
    private Integer idEstudiante;

    @NotBlank
    private String titulo;

    @NotBlank
    private String contenido;
}
