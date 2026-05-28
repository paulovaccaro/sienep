package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Observacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ObservacionRepository extends JpaRepository<Observacion, Integer> {

    List<Observacion> findByFuncionario_IdUsuario(Integer idFuncionario);

    List<Observacion> findByFuncionario_IdUsuarioAndEstudiante_IdUsuario(Integer idFuncionario, Integer idEstudiante);

    @Query("""
        SELECT o FROM Observacion o
        JOIN FETCH o.funcionario JOIN FETCH o.estudiante e JOIN FETCH e.grupo g
        WHERE o.idObservacion = :id
        """)
    Optional<Observacion> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
        SELECT o FROM Observacion o
        JOIN FETCH o.funcionario JOIN FETCH o.estudiante e JOIN FETCH e.grupo g
        WHERE e.idUsuario = :idEstudiante
        """)
    List<Observacion> findByEstudianteIdWithDetails(@Param("idEstudiante") Integer idEstudiante);

    long countByEstudiante_IdUsuario(Integer idEstudiante);
}
