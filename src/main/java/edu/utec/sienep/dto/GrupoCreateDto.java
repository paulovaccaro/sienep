package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GrupoCreateDto {

    @NotBlank
    private String nomGrupo;

    @NotNull
    private Integer idCarrera;

    @NotNull
    private Integer idItr;

    @NotNull
    private Integer anio;

    @NotNull
    private Integer semestre;
}
