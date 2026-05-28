package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "part_seguimientos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PartSeguimiento {

    @EmbeddedId
    private PartSeguimientoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idParticipante")
    @JoinColumn(name = "id_participante")
    private Usuario participante;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idSeguimiento")
    @JoinColumn(name = "id_seguimiento")
    private Seguimiento seguimiento;
}
