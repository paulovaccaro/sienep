package edu.utec.sienep.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tele_itr")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TeleITR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_telefono")
    private Integer idTelefono;

    @Column(name = "numero", nullable = false, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_itr", nullable = false)
    private ITR itr;
}
