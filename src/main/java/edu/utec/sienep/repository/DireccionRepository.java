package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionRepository extends JpaRepository<Direccion, Integer> {

    List<Direccion> findByUsuario_IdUsuario(Integer idUsuario);
}
