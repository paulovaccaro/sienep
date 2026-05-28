package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class EventoCalendarioCreateDto {

    @NotBlank
    private String titulo;

    private String descripcion;

    @NotNull
    private OffsetDateTime fecInicio;

    private OffsetDateTime fecFin;

    private String ubicacion;

    private Integer idInstancia;

    private Integer idRecordatorio;
}
