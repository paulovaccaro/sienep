package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grupos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_grupo")
    private Integer idGrupo;

    @Column(name = "nom_grupo", nullable = false, length = 50)
    private String nomGrupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrera", nullable = false)
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_itr", nullable = false)
    private ITR itr;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "semestre", nullable = false)
    private Integer semestre;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;
}
