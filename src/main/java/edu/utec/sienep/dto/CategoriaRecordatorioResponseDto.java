package edu.utec.sienep.dto;

import edu.utec.sienep.entity.CategoriaRecordatorio;
import lombok.Data;

@Data
public class CategoriaRecordatorioResponseDto {

    private Integer idCategoriaRecordatorio;
    private String nombre;
    private String descripcion;
    private Boolean estActivo;

    public static CategoriaRecordatorioResponseDto from(CategoriaRecordatorio c) {
        CategoriaRecordatorioResponseDto dto = new CategoriaRecordatorioResponseDto();
        dto.idCategoriaRecordatorio = c.getIdCategoriaRecordatorio();
        dto.nombre                  = c.getNombre();
        dto.descripcion             = c.getDescripcion();
        dto.estActivo               = c.getEstActivo();
        return dto;
    }
}
