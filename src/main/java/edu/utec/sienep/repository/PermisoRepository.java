package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermisoRepository extends JpaRepository<Permiso, Integer> {

    Optional<Permiso> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
