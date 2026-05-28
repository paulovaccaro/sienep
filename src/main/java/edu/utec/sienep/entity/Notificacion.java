package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "notificaciones")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_instancia", nullable = false)
    private Instancia instancia;

    @Column(name = "asunto", nullable = false, length = 200)
    private String asunto;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "destinatario", nullable = false, length = 100)
    private String destinatario;

    @Column(name = "fec_envio", nullable = false)
    private LocalDate fecEnvio;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;
}
