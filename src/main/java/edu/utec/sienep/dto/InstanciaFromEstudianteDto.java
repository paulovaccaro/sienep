package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class InstanciaFromEstudianteDto {

    @NotBlank
    private String titulo;

    @NotBlank
    private String tipo;

    @NotNull
    private OffsetDateTime fecHora;

    @NotBlank
    private String descripcion;

    private Integer idCategoria;
}
