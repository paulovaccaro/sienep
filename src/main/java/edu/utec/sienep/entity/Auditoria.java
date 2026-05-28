package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "auditoria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    @Column(name = "entidad", nullable = false, length = 50)
    private String entidad;

    @Column(name = "id_entidad", length = 50)
    private String idEntidad;

    @Column(name = "fec_hora", nullable = false)
    private OffsetDateTime fecHora;

    @Column(name = "detalle", columnDefinition = "jsonb")
    private String detalle;
}
