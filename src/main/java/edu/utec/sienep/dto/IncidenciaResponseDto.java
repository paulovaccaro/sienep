package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Incidencia;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class IncidenciaResponseDto {

    private Integer idInstancia;
    private String lugar;
    private String titulo;
    private String tipo;
    private OffsetDateTime fecHora;
    private String descripcion;
    private Boolean estActivo;
    private Integer idFuncionario;

    public static IncidenciaResponseDto from(Incidencia i) {
        IncidenciaResponseDto dto = new IncidenciaResponseDto();
        dto.idInstancia  = i.getIdInstancia();
        dto.lugar        = i.getLugar();
        dto.titulo       = i.getInstancia().getTitulo();
        dto.tipo         = i.getInstancia().getTipo();
        dto.fecHora      = i.getInstancia().getFecHora();
        dto.descripcion  = i.getInstancia().getDescripcion();
        dto.estActivo    = i.getInstancia().getEstActivo();
        dto.idFuncionario = i.getInstancia().getFuncionario().getIdUsuario();
        return dto;
    }
}
