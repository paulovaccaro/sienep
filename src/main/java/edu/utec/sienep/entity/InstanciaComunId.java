package edu.utec.sienep.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class InstanciaComunId implements Serializable {

    @Column(name = "id_instancia")
    private Integer idInstancia;

    @Column(name = "id_seguimiento")
    private Integer idSeguimiento;
}
