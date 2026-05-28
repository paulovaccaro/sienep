package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Integer> {

    @Query("SELECT g FROM Grupo g JOIN FETCH g.carrera JOIN FETCH g.itr WHERE g.estActivo = true")
    List<Grupo> findByEstActivo(Boolean estActivo);

    @Query("SELECT g FROM Grupo g JOIN FETCH g.carrera JOIN FETCH g.itr WHERE g.itr.idItr = :idItr")
    List<Grupo> findByItr_IdItr(@Param("idItr") Integer idItr);

    List<Grupo> findByCarrera_IdCarrera(Integer idCarrera);

    List<Grupo> findByItr_IdItrAndCarrera_IdCarreraAndAnioAndSemestre(
            Integer idItr, Integer idCarrera, Integer anio, Integer semestre);

    @Query("SELECT g FROM Grupo g JOIN FETCH g.carrera JOIN FETCH g.itr WHERE g.idGrupo = :id")
    Optional<Grupo> findByIdWithDetails(@Param("id") Integer id);
}
