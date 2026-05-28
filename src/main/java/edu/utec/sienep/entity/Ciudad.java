package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ciudades")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Ciudad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ciudad")
    private Integer idCiudad;

    @Column(name = "cod_postal", nullable = false, unique = true)
    private Integer codPostal;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "departamento", nullable = false, length = 100)
    private String departamento;
}
