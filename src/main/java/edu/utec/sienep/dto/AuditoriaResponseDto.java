package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Auditoria;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor
public class AuditoriaResponseDto {

    private Long idAuditoria;
    private Integer idUsuario;
    private String nombreUsuario;
    private String accion;
    private String entidad;
    private String idEntidad;
    private OffsetDateTime fecHora;
    private String detalle;

    public static AuditoriaResponseDto from(Auditoria a) {
        AuditoriaResponseDto dto = new AuditoriaResponseDto();
        dto.setIdAuditoria(a.getIdAuditoria());
        dto.setAccion(a.getAccion());
        dto.setEntidad(a.getEntidad());
        dto.setIdEntidad(a.getIdEntidad());
        dto.setFecHora(a.getFecHora());
        dto.setDetalle(a.getDetalle());
        if (a.getUsuario() != null) {
            dto.setIdUsuario(a.getUsuario().getIdUsuario());
            dto.setNombreUsuario(a.getUsuario().getNombre() + " " + a.getUsuario().getApellido());
        }
        return dto;
    }
}
