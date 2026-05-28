package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Notificacion;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NotificacionResponseDto {

    private Integer idNotificacion;
    private Integer idInstancia;
    private String asunto;
    private String mensaje;
    private String destinatario;
    private LocalDate fecEnvio;
    private Boolean estActivo;

    public static NotificacionResponseDto from(Notificacion n) {
        NotificacionResponseDto dto = new NotificacionResponseDto();
        dto.idNotificacion = n.getIdNotificacion();
        dto.idInstancia    = n.getInstancia().getIdInstancia();
        dto.asunto         = n.getAsunto();
        dto.mensaje        = n.getMensaje();
        dto.destinatario   = n.getDestinatario();
        dto.fecEnvio       = n.getFecEnvio();
        dto.estActivo      = n.getEstActivo();
        return dto;
    }
}
