package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pertenece")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Pertenece {

    @EmbeddedId
    private PerteneceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idCarrera")
    @JoinColumn(name = "id_carrera")
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idItr")
    @JoinColumn(name = "id_itr")
    private ITR itr;
}
