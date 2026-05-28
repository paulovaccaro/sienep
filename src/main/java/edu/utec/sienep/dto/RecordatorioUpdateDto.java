package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Recurrencia;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class RecordatorioUpdateDto {

    private String titulo;
    private String descripcion;
    private OffsetDateTime fecHora;
    private Recurrencia recurrencia;
    private Integer idCategoria;
    private Boolean estActivo;
}
