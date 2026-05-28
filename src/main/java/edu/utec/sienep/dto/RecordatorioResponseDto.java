package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Recordatorio;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class RecordatorioResponseDto {

    private Integer idRecordatorio;
    private String titulo;
    private String descripcion;
    private OffsetDateTime fecHora;
    private String recurrencia;
    private Integer idFuncionario;
    private String nombreFuncionario;
    private Integer idEstudiante;
    private String nombreEstudiante;
    private Integer idCategoria;
    private String nombreCategoria;
    private Boolean estActivo;

    public static RecordatorioResponseDto from(Recordatorio r) {
        RecordatorioResponseDto dto = new RecordatorioResponseDto();
        dto.idRecordatorio    = r.getIdRecordatorio();
        dto.titulo            = r.getTitulo();
        dto.descripcion       = r.getDescripcion();
        dto.fecHora           = r.getFecHora();
        dto.recurrencia       = r.getRecurrencia().name();
        dto.idFuncionario     = r.getFuncionario().getIdUsuario();
        dto.nombreFuncionario = r.getFuncionario().getUsuario().getNombre()
                              + " " + r.getFuncionario().getUsuario().getApellido();
        dto.idEstudiante      = r.getEstudiante() != null ? r.getEstudiante().getIdUsuario() : null;
        dto.nombreEstudiante  = r.getEstudiante() != null
                ? r.getEstudiante().getUsuario().getNombre() + " " + r.getEstudiante().getUsuario().getApellido()
                : null;
        dto.idCategoria       = r.getCategoria() != null ? r.getCategoria().getIdCategoriaRecordatorio() : null;
        dto.nombreCategoria   = r.getCategoria() != null ? r.getCategoria().getNombre() : null;
        dto.estActivo         = r.getEstActivo();
        return dto;
    }
}
