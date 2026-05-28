package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "arch_adjuntos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ArchivoAdjunto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_archivo_adjunto")
    private Integer idArchivoAdjunto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    @Column(name = "ruta", nullable = false, length = 255)
    private String ruta;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;
}
