package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorias_recordatorio")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CategoriaRecordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria_recordatorio")
    private Integer idCategoriaRecordatorio;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;
}
