package edu.utec.sienep.dto;

import edu.utec.sienep.entity.ArchivoAdjunto;
import lombok.Data;

@Data
public class ArchivoAdjuntoResponseDto {

    private Integer idArchivoAdjunto;
    private Integer idUsuario;
    private Integer idEstudiante;
    private Integer idGrupo;
    private String ruta;
    private String categoria;
    private Boolean estActivo;

    public static ArchivoAdjuntoResponseDto from(ArchivoAdjunto a) {
        ArchivoAdjuntoResponseDto dto = new ArchivoAdjuntoResponseDto();
        dto.idArchivoAdjunto = a.getIdArchivoAdjunto();
        dto.idUsuario        = a.getUsuario().getIdUsuario();
        dto.idEstudiante     = a.getEstudiante().getIdUsuario();
        dto.idGrupo          = a.getEstudiante().getGrupo().getIdGrupo();
        dto.ruta             = a.getRuta();
        dto.categoria        = a.getCategoria();
        dto.estActivo        = a.getEstActivo();
        return dto;
    }
}
