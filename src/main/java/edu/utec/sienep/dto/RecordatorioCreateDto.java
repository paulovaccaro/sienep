package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Recurrencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class RecordatorioCreateDto {

    @NotBlank
    private String titulo;

    private String descripcion;

    @NotNull
    private OffsetDateTime fecHora;

    private Recurrencia recurrencia = Recurrencia.NINGUNA;

    private Integer idEstudiante;
    private Integer idCategoria;
}
