package edu.utec.sienep.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class PartInstanciaId implements Serializable {

    @Column(name = "id_participante")
    private Integer idParticipante;

    @Column(name = "id_instancia")
    private Integer idInstancia;
}
