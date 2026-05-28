package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "recordatorios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Recordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recordatorio")
    private Integer idRecordatorio;

    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "fec_hora", nullable = false)
    private OffsetDateTime fecHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrencia", nullable = false, length = 20)
    private Recurrencia recurrencia = Recurrencia.NINGUNA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionario", nullable = false)
    private Funcionario funcionario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante")
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private CategoriaRecordatorio categoria;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;
}
