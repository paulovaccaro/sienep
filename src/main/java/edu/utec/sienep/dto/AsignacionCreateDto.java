package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignacionCreateDto {

    @NotNull
    private Integer idUsuario;

    @NotNull
    private Integer idRol;

    private Integer idItr;
    private Integer idCarrera;
    private Integer idGrupo;
}
