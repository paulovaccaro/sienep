package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Observacion;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ObservacionResponseDto {

    private Integer idObservacion;
    private Integer idFuncionario;
    private Integer idEstudiante;
    private Integer idGrupo;
    private String titulo;
    private String contenido;
    private OffsetDateTime fecHora;
    private Boolean estActivo;

    public static ObservacionResponseDto from(Observacion o) {
        ObservacionResponseDto dto = new ObservacionResponseDto();
        dto.idObservacion  = o.getIdObservacion();
        dto.idFuncionario  = o.getFuncionario().getIdUsuario();
        dto.idEstudiante   = o.getEstudiante().getIdUsuario();
        dto.idGrupo        = o.getEstudiante().getGrupo().getIdGrupo();
        dto.titulo         = o.getTitulo();
        dto.contenido      = o.getContenido();
        dto.fecHora        = o.getFecHora();
        dto.estActivo      = o.getEstActivo();
        return dto;
    }
}
