package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inst_comun")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InstanciaComun {

    @EmbeddedId
    private InstanciaComunId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idInstancia")
    @JoinColumn(name = "id_instancia")
    private Instancia instancia;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idSeguimiento")
    @JoinColumn(name = "id_seguimiento")
    private Seguimiento seguimiento;
}
