package edu.utec.sienep.repository;

import edu.utec.sienep.entity.EventoCalendario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoCalendarioRepository extends JpaRepository<EventoCalendario, Integer> {

    List<EventoCalendario> findByInstancia_IdInstancia(Integer idInstancia);

    List<EventoCalendario> findByRecordatorio_IdRecordatorio(Integer idRecordatorio);

    List<EventoCalendario> findBySincronizado(Boolean sincronizado);
}
