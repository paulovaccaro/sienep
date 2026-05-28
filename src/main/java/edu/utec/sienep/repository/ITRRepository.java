package edu.utec.sienep.repository;

import edu.utec.sienep.entity.ITR;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ITRRepository extends JpaRepository<ITR, Integer> {

    Optional<ITR> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<ITR> findByEstActivo(Boolean estActivo);
}
