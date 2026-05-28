package edu.utec.sienep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificacionCreateDto {

    @NotNull
    private Integer idInstancia;

    @NotBlank
    private String asunto;

    @NotBlank
    private String mensaje;

    @NotBlank
    private String destinatario;
}
