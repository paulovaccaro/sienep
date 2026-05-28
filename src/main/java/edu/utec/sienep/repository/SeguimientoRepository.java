package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Seguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Integer> {

    List<Seguimiento> findByEstudiante_IdUsuarioAndEstActivo(Integer idEstudiante, Boolean estActivo);

    @Query("SELECT COUNT(s) > 0 FROM Seguimiento s WHERE s.estudiante.idUsuario = :idEstudiante AND s.estActivo = true")
    boolean tieneSeguimientoActivo(@Param("idEstudiante") Integer idEstudiante);

    @Query("""
        SELECT s FROM Seguimiento s
        JOIN FETCH s.estudiante e JOIN FETCH e.usuario JOIN FETCH e.grupo g
        JOIN FETCH g.itr JOIN FETCH g.carrera
        WHERE s.idSeguimiento = :id
        """)
    Optional<Seguimiento> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
        SELECT s FROM Seguimiento s
        JOIN FETCH s.estudiante e JOIN FETCH e.usuario JOIN FETCH e.grupo g
        JOIN FETCH g.itr JOIN FETCH g.carrera
        WHERE e.idUsuario = :idEstudiante
        """)
    List<Seguimiento> findByEstudianteIdWithDetails(@Param("idEstudiante") Integer idEstudiante);

    @Query("""
        SELECT s FROM Seguimiento s
        JOIN FETCH s.estudiante e JOIN FETCH e.usuario JOIN FETCH e.grupo g
        JOIN FETCH g.itr JOIN FETCH g.carrera
        WHERE g.idGrupo IN :idGrupos
        """)
    List<Seguimiento> findByGruposIn(@Param("idGrupos") List<Integer> idGrupos);

    @Query("""
        SELECT s FROM Seguimiento s
        JOIN FETCH s.estudiante e JOIN FETCH e.usuario JOIN FETCH e.grupo g
        JOIN FETCH g.itr JOIN FETCH g.carrera
        """)
    List<Seguimiento> findAllWithDetails();

    long countByEstudiante_IdUsuario(Integer idEstudiante);
}
