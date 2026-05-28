package edu.utec.sienep.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class InformeFinalCreateDto {

    @NotBlank
    private String contenido;

    @NotNull
    @Min(1) @Max(10)
    private Integer valoracion;

    private LocalDate fecCreacion;
}
