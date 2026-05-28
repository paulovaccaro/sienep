package edu.utec.sienep.dto;

import edu.utec.sienep.entity.ITR;
import lombok.Data;

@Data
public class ITRResponseDto {

    private Integer idItr;
    private String codigo;
    private String nombre;
    private Integer idDireccion;
    private Boolean estActivo;

    public static ITRResponseDto from(ITR itr) {
        ITRResponseDto dto = new ITRResponseDto();
        dto.idItr       = itr.getIdItr();
        dto.codigo      = itr.getCodigo();
        dto.nombre      = itr.getNombre();
        dto.idDireccion = itr.getDireccion().getIdDireccion();
        dto.estActivo   = itr.getEstActivo();
        return dto;
    }
}
