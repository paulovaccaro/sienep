package edu.utec.sienep.dto;

import lombok.Data;

@Data
public class AsignacionUpdateDto {

    private Integer idRol;
    private Integer idItr;
    private Integer idCarrera;
    private Integer idGrupo;
    private Boolean estActivo;
    private Boolean limpiarItr;
    private Boolean limpiarCarrera;
    private Boolean limpiarGrupo;
}
