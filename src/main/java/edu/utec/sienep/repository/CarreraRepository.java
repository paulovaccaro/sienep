package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

    Optional<Carrera> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<Carrera> findByEstActivo(Boolean estActivo);
}
