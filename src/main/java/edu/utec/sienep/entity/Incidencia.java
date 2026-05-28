package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "incidencias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Incidencia {

    @Id
    @Column(name = "id_instancia")
    private Integer idInstancia;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_instancia")
    private Instancia instancia;

    @Column(name = "lugar", nullable = false, length = 100)
    private String lugar;
}
