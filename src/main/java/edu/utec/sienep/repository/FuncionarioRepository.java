package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuncionarioRepository extends JpaRepository<Funcionario, Integer> {

    List<Funcionario> findByEstActivo(Boolean estActivo);
}
