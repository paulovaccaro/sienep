package edu.utec.sienep.repository;

import edu.utec.sienep.entity.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AsignacionRepository extends JpaRepository<Asignacion, Integer> {

    @Query("""
        SELECT a FROM Asignacion a
        JOIN FETCH a.usuario JOIN FETCH a.rol
        LEFT JOIN FETCH a.itr LEFT JOIN FETCH a.carrera LEFT JOIN FETCH a.grupo
        WHERE a.idAsignacion = :id
        """)
    Optional<Asignacion> findByIdWithDetails(@Param("id") Integer id);

    @Query("""
        SELECT a FROM Asignacion a
        JOIN FETCH a.usuario JOIN FETCH a.rol
        LEFT JOIN FETCH a.itr LEFT JOIN FETCH a.carrera LEFT JOIN FETCH a.grupo
        WHERE a.usuario.idUsuario = :idUsuario
        """)
    List<Asignacion> findByUsuarioWithDetails(@Param("idUsuario") Integer idUsuario);

    List<Asignacion> findByUsuario_IdUsuarioAndEstActivo(Integer idUsuario, Boolean estActivo);

    @Query("""
        SELECT COUNT(a) > 0
        FROM Asignacion a
        JOIN RolPermiso rp ON rp.rol = a.rol
        JOIN rp.permiso p
        JOIN Grupo g ON g.idGrupo = :idGrupo
        WHERE a.usuario.idUsuario = :idUsuario
          AND a.estActivo = true
          AND p.codigo = :codigoPermiso
          AND (a.grupo IS NULL OR a.grupo.idGrupo = g.idGrupo)
          AND (a.itr IS NULL OR a.itr.idItr = g.itr.idItr)
          AND (a.carrera IS NULL OR a.carrera.idCarrera = g.carrera.idCarrera)
        """)
    boolean tienePermisoEnGrupo(
            @Param("idUsuario") Integer idUsuario,
            @Param("codigoPermiso") String codigoPermiso,
            @Param("idGrupo") Integer idGrupo);

    @Query("""
        SELECT COUNT(a) > 0
        FROM Asignacion a
        JOIN RolPermiso rp ON rp.rol = a.rol
        JOIN rp.permiso p
        WHERE a.usuario.idUsuario = :idUsuario
          AND a.estActivo = true
          AND p.codigo = :codigoPermiso
          AND a.grupo IS NULL
          AND a.itr IS NULL
          AND a.carrera IS NULL
        """)
    boolean tienePermisoGlobal(
            @Param("idUsuario") Integer idUsuario,
            @Param("codigoPermiso") String codigoPermiso);

    @Query("""
        SELECT g.idGrupo FROM Grupo g
        WHERE EXISTS (
            SELECT 1 FROM Asignacion a
            JOIN RolPermiso rp ON rp.rol = a.rol
            JOIN rp.permiso p
            WHERE a.usuario.idUsuario = :idUsuario
              AND a.estActivo = true
              AND p.codigo = :codigoPermiso
              AND (a.grupo IS NULL OR a.grupo.idGrupo = g.idGrupo)
              AND (a.itr IS NULL OR a.itr.idItr = g.itr.idItr)
              AND (a.carrera IS NULL OR a.carrera.idCarrera = g.carrera.idCarrera)
        )
        """)
    List<Integer> findGruposAccesibles(
            @Param("idUsuario") Integer idUsuario,
            @Param("codigoPermiso") String codigoPermiso);
}
