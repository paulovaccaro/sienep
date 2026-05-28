package edu.utec.sienep.dto;

import lombok.Data;

@Data
public class EstudianteUpdateDto {

    private String nombre;
    private String apellido;
    private Integer idGrupo;
    private Boolean estActivo;
}
