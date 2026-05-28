package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SeguimientoCreateDto {

    private Integer idInforme;

    @NotNull
    private Integer idEstudiante;

    @NotNull
    private LocalDate fecInicio;

    private LocalDate fecCierre;

    private boolean estActivo = true;
}
