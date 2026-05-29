package edu.utec.sienep.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Schema(description = "Datos para registrar una instancia (RF10)")
public class InstanciaCreateDto {

    @NotBlank
    @Schema(description = "Título descriptivo de la instancia", example = "Reunión de seguimiento semestral")
    private String titulo;

    @NotBlank
    @Schema(description = "Tipo de instancia", example = "Seguimiento")
    private String tipo;

    @NotNull
    @Schema(description = "Fecha y hora de la instancia (ISO 8601 con zona horaria)", example = "2025-09-01T10:00:00-03:00")
    private OffsetDateTime fecHora;

    @NotBlank
    @Schema(description = "Descripción detallada de la instancia", example = "Revisión del avance académico del estudiante")
    private String descripcion;

    @NotNull
    @Schema(description = "ID del funcionario responsable", example = "1")
    private Integer idFuncionario;

    @Schema(description = "ID de la categoría de instancia (opcional)", example = "2")
    private Integer idCategoria;
}
