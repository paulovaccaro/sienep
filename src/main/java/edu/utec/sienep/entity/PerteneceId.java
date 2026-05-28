package edu.utec.sienep.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class PerteneceId implements Serializable {

    @Column(name = "id_carrera")
    private Integer idCarrera;

    @Column(name = "id_itr")
    private Integer idItr;
}
