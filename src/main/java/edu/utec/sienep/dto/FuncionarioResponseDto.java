package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Funcionario;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FuncionarioResponseDto {

    private Integer idUsuario;
    private String cedula;
    private String nombre;
    private String apellido;
    private String username;
    private String correo;
    private LocalDate fecNacimiento;
    private Boolean estActivo;

    public static FuncionarioResponseDto from(Funcionario f) {
        FuncionarioResponseDto dto = new FuncionarioResponseDto();
        dto.idUsuario    = f.getIdUsuario();
        dto.cedula       = f.getUsuario().getCedula();
        dto.nombre       = f.getUsuario().getNombre();
        dto.apellido     = f.getUsuario().getApellido();
        dto.username     = f.getUsuario().getUsername();
        dto.correo       = f.getUsuario().getCorreo();
        dto.fecNacimiento = f.getUsuario().getFecNacimiento();
        dto.estActivo    = f.getEstActivo();
        return dto;
    }
}
