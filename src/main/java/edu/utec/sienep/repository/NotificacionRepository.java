package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    List<Notificacion> findByInstancia_IdInstancia(Integer idInstancia);
}
