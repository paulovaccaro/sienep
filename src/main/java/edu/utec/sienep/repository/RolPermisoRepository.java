package edu.utec.sienep.repository;

import edu.utec.sienep.entity.RolPermiso;
import edu.utec.sienep.entity.RolPermisoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, RolPermisoId> {
}
