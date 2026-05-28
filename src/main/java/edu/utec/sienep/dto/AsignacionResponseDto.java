package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Asignacion;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class AsignacionResponseDto {

    private Integer idAsignacion;
    private Integer idUsuario;
    private String username;
    private Integer idRol;
    private String nombreRol;
    private Integer idItr;
    private String nombreItr;
    private Integer idCarrera;
    private String nombreCarrera;
    private Integer idGrupo;
    private String nombreGrupo;
    private OffsetDateTime fecCreacion;
    private Boolean estActivo;

    public static AsignacionResponseDto from(Asignacion a) {
        AsignacionResponseDto dto = new AsignacionResponseDto();
        dto.idAsignacion  = a.getIdAsignacion();
        dto.idUsuario     = a.getUsuario().getIdUsuario();
        dto.username      = a.getUsuario().getUsername();
        dto.idRol         = a.getRol().getIdRol();
        dto.nombreRol     = a.getRol().getNombre();
        dto.idItr         = a.getItr()     != null ? a.getItr().getIdItr()         : null;
        dto.nombreItr     = a.getItr()     != null ? a.getItr().getNombre()         : null;
        dto.idCarrera     = a.getCarrera() != null ? a.getCarrera().getIdCarrera()  : null;
        dto.nombreCarrera = a.getCarrera() != null ? a.getCarrera().getNombre()     : null;
        dto.idGrupo       = a.getGrupo()   != null ? a.getGrupo().getIdGrupo()     : null;
        dto.nombreGrupo   = a.getGrupo()   != null ? a.getGrupo().getNomGrupo()    : null;
        dto.fecCreacion   = a.getFecCreacion();
        dto.estActivo     = a.getEstActivo();
        return dto;
    }
}
