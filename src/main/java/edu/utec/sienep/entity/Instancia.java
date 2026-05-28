package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "instancias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Instancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_instancia")
    private Integer idInstancia;

    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @Column(name = "tipo", nullable = false, length = 100)
    private String tipo;

    @Column(name = "fec_hora", nullable = false)
    private OffsetDateTime fecHora;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_funcionario", nullable = false)
    private Funcionario funcionario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_instancia")
    private CategoriaInstancia categoria;
}
