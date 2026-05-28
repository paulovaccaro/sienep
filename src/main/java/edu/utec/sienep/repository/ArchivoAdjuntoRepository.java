package edu.utec.sienep.repository;

import edu.utec.sienep.entity.ArchivoAdjunto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArchivoAdjuntoRepository extends JpaRepository<ArchivoAdjunto, Integer> {

    List<ArchivoAdjunto> findByEstudiante_IdUsuarioAndEstActivo(Integer idEstudiante, Boolean estActivo);

    @Query("""
        SELECT a FROM ArchivoAdjunto a
        JOIN FETCH a.usuario JOIN FETCH a.estudiante e JOIN FETCH e.grupo g
        WHERE a.idArchivoAdjunto = :id
        """)
    Optional<ArchivoAdjunto> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
        SELECT a FROM ArchivoAdjunto a
        JOIN FETCH a.usuario JOIN FETCH a.estudiante e JOIN FETCH e.grupo g
        WHERE e.idUsuario = :idEstudiante
        """)
    List<ArchivoAdjunto> findByEstudianteIdWithDetails(@Param("idEstudiante") Integer idEstudiante);
}
