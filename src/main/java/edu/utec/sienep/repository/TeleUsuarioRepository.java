package edu.utec.sienep.repository;

import edu.utec.sienep.entity.TeleUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeleUsuarioRepository extends JpaRepository<TeleUsuario, Integer> {

    List<TeleUsuario> findByUsuario_IdUsuario(Integer idUsuario);
}
