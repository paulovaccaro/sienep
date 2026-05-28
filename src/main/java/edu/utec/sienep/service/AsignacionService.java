package edu.utec.sienep.service;

import edu.utec.sienep.dto.AsignacionResponseDto;
import edu.utec.sienep.entity.*;
import edu.utec.sienep.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AsignacionService {

    private final AsignacionRepository asignacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ITRRepository itrRepository;
    private final CarreraRepository carreraRepository;
    private final GrupoRepository grupoRepository;

    @Transactional
    public Asignacion crear(Integer idUsuario, Integer idRol,
                            Integer idItr, Integer idCarrera, Integer idGrupo) {

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + idRol));

        ITR itr = idItr != null
                ? itrRepository.findById(idItr).orElseThrow(() -> new IllegalArgumentException("ITR no encontrado: " + idItr))
                : null;
        Carrera carrera = idCarrera != null
                ? carreraRepository.findById(idCarrera).orElseThrow(() -> new IllegalArgumentException("Carrera no encontrada: " + idCarrera))
                : null;
        Grupo grupo = idGrupo != null
                ? grupoRepository.findById(idGrupo).orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo))
                : null;

        Asignacion a = new Asignacion();
        a.setUsuario(usuario);
        a.setRol(rol);
        a.setItr(itr);
        a.setCarrera(carrera);
        a.setGrupo(grupo);
        a.setFecCreacion(OffsetDateTime.now());
        a.setEstActivo(true);
        return asignacionRepository.save(a);
    }

    @Transactional(readOnly = true)
    public Asignacion obtenerPorId(Integer id) {
        return asignacionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<Asignacion> listarPorUsuario(Integer idUsuario) {
        return asignacionRepository.findByUsuarioWithDetails(idUsuario);
    }

    @Transactional
    public Asignacion actualizar(Integer id, Integer idRol,
                                 Integer idItr, Boolean limpiarItr,
                                 Integer idCarrera, Boolean limpiarCarrera,
                                 Integer idGrupo, Boolean limpiarGrupo,
                                 Boolean estActivo) {

        Asignacion a = obtenerPorId(id);

        if (idRol != null) {
            Rol rol = rolRepository.findById(idRol)
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + idRol));
            a.setRol(rol);
        }

        if (idItr != null) {
            ITR itr = itrRepository.findById(idItr)
                    .orElseThrow(() -> new IllegalArgumentException("ITR no encontrado: " + idItr));
            a.setItr(itr);
        } else if (Boolean.TRUE.equals(limpiarItr)) {
            a.setItr(null);
        }

        if (idCarrera != null) {
            Carrera carrera = carreraRepository.findById(idCarrera)
                    .orElseThrow(() -> new IllegalArgumentException("Carrera no encontrada: " + idCarrera));
            a.setCarrera(carrera);
        } else if (Boolean.TRUE.equals(limpiarCarrera)) {
            a.setCarrera(null);
        }

        if (idGrupo != null) {
            Grupo grupo = grupoRepository.findById(idGrupo)
                    .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));
            a.setGrupo(grupo);
        } else if (Boolean.TRUE.equals(limpiarGrupo)) {
            a.setGrupo(null);
        }

        if (estActivo != null) a.setEstActivo(estActivo);

        return asignacionRepository.save(a);
    }

    @Transactional
    public void desactivar(Integer id) {
        Asignacion a = obtenerPorId(id);
        a.setEstActivo(false);
        asignacionRepository.save(a);
    }

    @Transactional(readOnly = true)
    public List<AsignacionResponseDto> listarPorUsuarioDto(Integer idUsuario) {
        return listarPorUsuario(idUsuario).stream().map(AsignacionResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public AsignacionResponseDto obtenerDtoPorId(Integer id) {
        return AsignacionResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public AsignacionResponseDto crearDto(Integer idUsuario, Integer idRol,
                                          Integer idItr, Integer idCarrera, Integer idGrupo) {
        return AsignacionResponseDto.from(crear(idUsuario, idRol, idItr, idCarrera, idGrupo));
    }

    @Transactional
    public AsignacionResponseDto actualizarDto(Integer id, Integer idRol,
                                               Integer idItr, Boolean limpiarItr,
                                               Integer idCarrera, Boolean limpiarCarrera,
                                               Integer idGrupo, Boolean limpiarGrupo,
                                               Boolean estActivo) {
        return AsignacionResponseDto.from(actualizar(id, idRol, idItr, limpiarItr,
                idCarrera, limpiarCarrera, idGrupo, limpiarGrupo, estActivo));
    }
}
