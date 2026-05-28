package edu.utec.sienep.dto;

import edu.utec.sienep.entity.EventoCalendario;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class EventoCalendarioResponseDto {

    private Integer idEventoCalendario;
    private String titulo;
    private String descripcion;
    private OffsetDateTime fecInicio;
    private OffsetDateTime fecFin;
    private String ubicacion;
    private Integer idInstancia;
    private Integer idRecordatorio;
    private String googleEventId;
    private String googleCalendarLink;
    private Boolean sincronizado;
    private Boolean estActivo;

    public static EventoCalendarioResponseDto from(EventoCalendario e) {
        EventoCalendarioResponseDto dto = new EventoCalendarioResponseDto();
        dto.idEventoCalendario  = e.getIdEventoCalendario();
        dto.titulo              = e.getTitulo();
        dto.descripcion         = e.getDescripcion();
        dto.fecInicio           = e.getFecInicio();
        dto.fecFin              = e.getFecFin();
        dto.ubicacion           = e.getUbicacion();
        dto.googleEventId       = e.getGoogleEventId();
        dto.googleCalendarLink  = e.getGoogleCalendarLink();
        dto.sincronizado        = e.getSincronizado();
        dto.estActivo           = e.getEstActivo();
        if (e.getInstancia() != null)
            dto.idInstancia = e.getInstancia().getIdInstancia();
        if (e.getRecordatorio() != null)
            dto.idRecordatorio = e.getRecordatorio().getIdRecordatorio();
        return dto;
    }
}
