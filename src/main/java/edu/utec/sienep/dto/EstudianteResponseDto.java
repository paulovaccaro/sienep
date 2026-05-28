package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Estudiante;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EstudianteResponseDto {

    private Integer idUsuario;
    private String cedula;
    private String nombre;
    private String apellido;
    private String username;
    private String correo;
    private LocalDate fecNacimiento;
    private Integer idGrupo;
    private String nomGrupo;
    private Boolean estActivo;

    public static EstudianteResponseDto from(Estudiante e) {
        EstudianteResponseDto dto = new EstudianteResponseDto();
        dto.idUsuario    = e.getIdUsuario();
        dto.cedula       = e.getUsuario().getCedula();
        dto.nombre       = e.getUsuario().getNombre();
        dto.apellido     = e.getUsuario().getApellido();
        dto.username     = e.getUsuario().getUsername();
        dto.correo       = e.getUsuario().getCorreo();
        dto.fecNacimiento = e.getUsuario().getFecNacimiento();
        dto.idGrupo      = e.getGrupo().getIdGrupo();
        dto.nomGrupo     = e.getGrupo().getNomGrupo();
        dto.estActivo    = e.getEstActivo();
        return dto;
    }
}
