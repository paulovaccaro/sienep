package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findByEntidadAndIdEntidadOrderByFecHoraDesc(String entidad, String idEntidad);

    List<Auditoria> findByUsuario_IdUsuarioOrderByFecHoraDesc(Integer idUsuario);
}
