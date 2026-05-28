package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "seguimientos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Seguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento")
    private Integer idSeguimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_informe")
    private InformeFinal informe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    @Column(name = "fec_inicio", nullable = false)
    private LocalDate fecInicio;

    @Column(name = "fec_cierre")
    private LocalDate fecCierre;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;
}
