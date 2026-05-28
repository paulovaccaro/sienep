package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "itr")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ITR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_itr")
    private Integer idItr;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_direccion", nullable = false)
    private Direccion direccion;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;
}
