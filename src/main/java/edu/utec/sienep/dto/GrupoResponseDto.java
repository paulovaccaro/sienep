package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Grupo;
import lombok.Data;

@Data
public class GrupoResponseDto {

    private Integer idGrupo;
    private String nomGrupo;
    private Integer idCarrera;
    private String nomCarrera;
    private Integer idItr;
    private String nomItr;
    private Integer anio;
    private Integer semestre;
    private Boolean estActivo;

    public static GrupoResponseDto from(Grupo g) {
        GrupoResponseDto dto = new GrupoResponseDto();
        dto.idGrupo   = g.getIdGrupo();
        dto.nomGrupo  = g.getNomGrupo();
        dto.idCarrera = g.getCarrera().getIdCarrera();
        dto.nomCarrera = g.getCarrera().getNombre();
        dto.idItr     = g.getItr().getIdItr();
        dto.nomItr    = g.getItr().getNombre();
        dto.anio      = g.getAnio();
        dto.semestre  = g.getSemestre();
        dto.estActivo = g.getEstActivo();
        return dto;
    }
}
