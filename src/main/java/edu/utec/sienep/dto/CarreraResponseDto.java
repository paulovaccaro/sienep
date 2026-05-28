package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Carrera;
import lombok.Data;

@Data
public class CarreraResponseDto {

    private Integer idCarrera;
    private String codigo;
    private String nombre;
    private String plan;
    private Boolean estActivo;

    public static CarreraResponseDto from(Carrera c) {
        CarreraResponseDto dto = new CarreraResponseDto();
        dto.idCarrera = c.getIdCarrera();
        dto.codigo    = c.getCodigo();
        dto.nombre    = c.getNombre();
        dto.plan      = c.getPlan();
        dto.estActivo = c.getEstActivo();
        return dto;
    }
}
