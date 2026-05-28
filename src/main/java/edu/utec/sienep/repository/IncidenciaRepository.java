package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Integer> {

    @Query("""
        SELECT i FROM Incidencia i
        JOIN FETCH i.instancia inst JOIN FETCH inst.funcionario
        WHERE i.idInstancia = :id
        """)
    Optional<Incidencia> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
        SELECT i FROM Incidencia i
        JOIN FETCH i.instancia inst JOIN FETCH inst.funcionario
        """)
    List<Incidencia> findAllWithDetails();

    @Query("""
        SELECT i FROM Incidencia i
        JOIN FETCH i.instancia inst JOIN FETCH inst.funcionario
        WHERE inst.funcionario.idUsuario = :idFuncionario
        """)
    List<Incidencia> findByFuncionarioWithDetails(@Param("idFuncionario") Integer idFuncionario);
}
