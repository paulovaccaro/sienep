package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "eventos_calendario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EventoCalendario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento_calendario")
    private Integer idEventoCalendario;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fec_inicio", nullable = false)
    private OffsetDateTime fecInicio;

    @Column(name = "fec_fin")
    private OffsetDateTime fecFin;

    @Column(name = "ubicacion", length = 255)
    private String ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_instancia")
    private Instancia instancia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recordatorio")
    private Recordatorio recordatorio;

    @Column(name = "google_event_id", nullable = false, length = 100)
    private String googleEventId;

    @Column(name = "google_calendar_link", nullable = false, length = 500)
    private String googleCalendarLink;

    @Column(name = "sincronizado", nullable = false)
    private Boolean sincronizado = false;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;
}
