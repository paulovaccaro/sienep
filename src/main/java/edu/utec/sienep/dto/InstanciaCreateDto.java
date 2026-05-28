package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class InstanciaCreateDto {

    @NotBlank
    private String titulo;

    @NotBlank
    private String tipo;

    @NotNull
    private OffsetDateTime fecHora;

    @NotBlank
    private String descripcion;

    @NotNull
    private Integer idFuncionario;

    private Integer idCategoria;
}
