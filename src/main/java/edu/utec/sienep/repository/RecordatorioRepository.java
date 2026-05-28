package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Recordatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface RecordatorioRepository extends JpaRepository<Recordatorio, Integer> {

    @Query("""
        SELECT r FROM Recordatorio r
        JOIN FETCH r.funcionario f JOIN FETCH f.usuario
        LEFT JOIN FETCH r.estudiante e LEFT JOIN FETCH e.usuario
        LEFT JOIN FETCH r.categoria
        WHERE r.idRecordatorio = :id
        """)
    Optional<Recordatorio> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
        SELECT r FROM Recordatorio r
        JOIN FETCH r.funcionario f JOIN FETCH f.usuario
        LEFT JOIN FETCH r.estudiante e LEFT JOIN FETCH e.usuario
        LEFT JOIN FETCH r.categoria
        """)
    List<Recordatorio> findAllWithDetails();

    @Query("""
        SELECT r FROM Recordatorio r
        JOIN FETCH r.funcionario f JOIN FETCH f.usuario
        LEFT JOIN FETCH r.estudiante e LEFT JOIN FETCH e.usuario
        LEFT JOIN FETCH r.categoria
        WHERE f.idUsuario = :idFuncionario
        """)
    List<Recordatorio> findByFuncionarioWithDetails(@Param("idFuncionario") Integer idFuncionario);

    @Query("""
        SELECT r FROM Recordatorio r
        JOIN FETCH r.funcionario f JOIN FETCH f.usuario
        LEFT JOIN FETCH r.estudiante e LEFT JOIN FETCH e.usuario
        WHERE r.fecHora >= :inicio AND r.fecHora < :fin
        ORDER BY r.fecHora
        """)
    List<Recordatorio> findByPeriodo(@Param("inicio") OffsetDateTime inicio,
                                     @Param("fin") OffsetDateTime fin);
}
