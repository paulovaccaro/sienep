package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByCedula(String cedula);

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByUsername(String username);

    boolean existsByCedula(String cedula);

    boolean existsByCorreo(String correo);
}
