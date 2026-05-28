package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "part_instancia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PartInstancia {

    @EmbeddedId
    private PartInstanciaId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idParticipante")
    @JoinColumn(name = "id_participante")
    private Usuario participante;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idInstancia")
    @JoinColumn(name = "id_instancia")
    private Instancia instancia;
}
