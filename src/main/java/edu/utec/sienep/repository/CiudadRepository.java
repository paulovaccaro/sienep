package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CiudadRepository extends JpaRepository<Ciudad, Integer> {

    Optional<Ciudad> findByCodPostal(Integer codPostal);
}
