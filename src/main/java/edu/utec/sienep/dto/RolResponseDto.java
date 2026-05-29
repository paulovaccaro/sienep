package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Rol;
import lombok.Data;

@Data
public class RolResponseDto {

    private Integer idRol;
    private String nombre;
    private String descripcion;
    private Boolean esSistema;
    private Boolean estActivo;

    public static RolResponseDto from(Rol rol) {
        RolResponseDto dto = new RolResponseDto();
        dto.idRol       = rol.getIdRol();
        dto.nombre      = rol.getNombre();
        dto.descripcion = rol.getDescripcion();
        dto.esSistema   = rol.getEsSistema();
        dto.estActivo   = rol.getEstActivo();
        return dto;
    }
}
