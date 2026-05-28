package edu.utec.sienep.dto;

import edu.utec.sienep.entity.CategoriaInstancia;
import lombok.Data;

@Data
public class CategoriaInstanciaResponseDto {

    private Integer idCategoriaInstancia;
    private String nombre;
    private String descripcion;
    private Boolean estActivo;

    public static CategoriaInstanciaResponseDto from(CategoriaInstancia c) {
        CategoriaInstanciaResponseDto dto = new CategoriaInstanciaResponseDto();
        dto.idCategoriaInstancia = c.getIdCategoriaInstancia();
        dto.nombre               = c.getNombre();
        dto.descripcion          = c.getDescripcion();
        dto.estActivo            = c.getEstActivo();
        return dto;
    }
}
