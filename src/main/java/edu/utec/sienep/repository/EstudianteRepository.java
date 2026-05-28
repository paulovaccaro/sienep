package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

    @Query("SELECT e FROM Estudiante e JOIN FETCH e.usuario JOIN FETCH e.grupo g JOIN FETCH g.carrera JOIN FETCH g.itr")
    List<Estudiante> findAll();

    @Query("SELECT e FROM Estudiante e JOIN FETCH e.usuario JOIN FETCH e.grupo g JOIN FETCH g.carrera JOIN FETCH g.itr WHERE e.idUsuario = :id")
    Optional<Estudiante> findById(@Param("id") Integer id);

    List<Estudiante> findByEstActivo(Boolean estActivo);

    List<Estudiante> findByGrupo_IdGrupo(Integer idGrupo);

    @Query("SELECT e FROM Estudiante e WHERE e.grupo.itr.idItr = :idItr AND e.estActivo = true")
    List<Estudiante> findActivosByItr(@Param("idItr") Integer idItr);

    @Query("SELECT e FROM Estudiante e JOIN FETCH e.usuario JOIN FETCH e.grupo g JOIN FETCH g.carrera JOIN FETCH g.itr WHERE g.idGrupo IN :idGrupos")
    List<Estudiante> findByGruposIn(@Param("idGrupos") List<Integer> idGrupos);
}
