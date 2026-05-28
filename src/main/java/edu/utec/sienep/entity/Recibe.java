package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recibe")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Recibe {

    @EmbeddedId
    private RecibeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idNotificacion")
    @JoinColumn(name = "id_notificacion")
    private Notificacion notificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario")
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}
