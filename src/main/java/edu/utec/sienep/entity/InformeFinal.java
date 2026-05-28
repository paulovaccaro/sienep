package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "info_final")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InformeFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inf_final")
    private Integer idInfFinal;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "valoracion", nullable = false)
    private Integer valoracion;

    @Column(name = "fec_creacion", nullable = false)
    private LocalDate fecCreacion;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;
}
