package edu.utec.sienep.repository;

import edu.utec.sienep.entity.PartInstancia;
import edu.utec.sienep.entity.PartInstanciaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PartInstanciaRepository extends JpaRepository<PartInstancia, PartInstanciaId> {

    List<PartInstancia> findByInstancia_IdInstancia(Integer idInstancia);
}
