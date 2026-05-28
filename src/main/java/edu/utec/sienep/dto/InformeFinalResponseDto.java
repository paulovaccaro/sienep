package edu.utec.sienep.dto;

import edu.utec.sienep.entity.InformeFinal;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InformeFinalResponseDto {

    private Integer idInfFinal;
    private String contenido;
    private Integer valoracion;
    private LocalDate fecCreacion;
    private Boolean estActivo;

    public static InformeFinalResponseDto from(InformeFinal i) {
        InformeFinalResponseDto dto = new InformeFinalResponseDto();
        dto.idInfFinal  = i.getIdInfFinal();
        dto.contenido   = i.getContenido();
        dto.valoracion  = i.getValoracion();
        dto.fecCreacion = i.getFecCreacion();
        dto.estActivo   = i.getEstActivo();
        return dto;
    }
}
