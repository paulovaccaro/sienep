package edu.utec.sienep.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class RecibeId implements Serializable {

    @Column(name = "id_notificacion")
    private Integer idNotificacion;

    @Column(name = "id_usuario")
    private Integer idUsuario;
}
