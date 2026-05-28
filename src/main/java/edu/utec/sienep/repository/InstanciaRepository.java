package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Instancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface InstanciaRepository extends JpaRepository<Instancia, Integer> {

    List<Instancia> findByFuncionario_IdUsuario(Integer idFuncionario);

    List<Instancia> findByTipoAndEstActivo(String tipo, Boolean estActivo);

    @Query("""
        SELECT i FROM Instancia i
        JOIN FETCH i.funcionario f JOIN FETCH f.usuario
        WHERE i.fecHora >= :inicio AND i.fecHora < :fin
        ORDER BY i.fecHora
        """)
    List<Instancia> findByPeriodo(@Param("inicio") OffsetDateTime inicio,
                                  @Param("fin") OffsetDateTime fin);
}
