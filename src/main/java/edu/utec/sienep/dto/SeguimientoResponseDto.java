package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Seguimiento;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SeguimientoResponseDto {

    private Integer idSeguimiento;
    private Integer idEstudiante;
    private Integer idGrupo;
    private Integer idInforme;
    private LocalDate fecInicio;
    private LocalDate fecCierre;
    private Boolean estActivo;

    public static SeguimientoResponseDto from(Seguimiento s) {
        SeguimientoResponseDto dto = new SeguimientoResponseDto();
        dto.idSeguimiento = s.getIdSeguimiento();
        dto.idEstudiante  = s.getEstudiante().getIdUsuario();
        dto.idGrupo       = s.getEstudiante().getGrupo().getIdGrupo();
        dto.idInforme     = s.getInforme() != null ? s.getInforme().getIdInfFinal() : null;
        dto.fecInicio     = s.getFecInicio();
        dto.fecCierre     = s.getFecCierre();
        dto.estActivo     = s.getEstActivo();
        return dto;
    }
}
