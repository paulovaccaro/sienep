package edu.utec.sienep.dto;

import edu.utec.sienep.entity.Instancia;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class InstanciaResponseDto {

    private Integer idInstancia;
    private String titulo;
    private String tipo;
    private OffsetDateTime fecHora;
    private String descripcion;
    private Boolean estActivo;
    private Integer idFuncionario;
    private Integer idCategoriaInstancia;
    private String nombreCategoria;

    public static InstanciaResponseDto from(Instancia i) {
        InstanciaResponseDto dto = new InstanciaResponseDto();
        dto.idInstancia   = i.getIdInstancia();
        dto.titulo        = i.getTitulo();
        dto.tipo          = i.getTipo();
        dto.fecHora       = i.getFecHora();
        dto.descripcion   = i.getDescripcion();
        dto.estActivo     = i.getEstActivo();
        dto.idFuncionario = i.getFuncionario().getIdUsuario();
        if (i.getCategoria() != null) {
            dto.idCategoriaInstancia = i.getCategoria().getIdCategoriaInstancia();
            dto.nombreCategoria      = i.getCategoria().getNombre();
        }
        return dto;
    }
}
