package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IncidenciaCreateDto {

    @NotNull
    private Integer idInstancia;

    @NotBlank
    private String lugar;
}
