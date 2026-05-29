package edu.utec.sienep.service;

import edu.utec.sienep.dto.AuditoriaResponseDto;
import edu.utec.sienep.entity.Auditoria;
import edu.utec.sienep.repository.AuditoriaRepository;
import edu.utec.sienep.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    /** Registra un evento de auditoría en transacción propia (no se pierde si la TX principal falla). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Integer idUsuarioActor, String accion, String entidad, String idEntidad, String detalle) {
        try {
            Auditoria a = new Auditoria();
            if (idUsuarioActor != null) {
                usuarioRepository.findById(idUsuarioActor).ifPresent(a::setUsuario);
            }
            a.setAccion(accion);
            a.setEntidad(entidad);
            a.setIdEntidad(idEntidad);
            a.setFecHora(OffsetDateTime.now());
            a.setDetalle(detalle);
            auditoriaRepository.save(a);
        } catch (Exception e) {
            log.warn("Error al registrar auditoría [{} {} {}]: {}", accion, entidad, idEntidad, e.getMessage());
        }
    }

    /** Variante que obtiene el actor del SecurityContext (peticiones autenticadas). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String accion, String entidad, String idEntidad, String detalle) {
        registrar(actorIdFromContext(), accion, entidad, idEntidad, detalle);
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponseDto> listarTodos() {
        return auditoriaRepository.findAllByOrderByFecHoraDesc()
                .stream().map(AuditoriaResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponseDto> listarPorEntidad(String entidad, String idEntidad) {
        return auditoriaRepository.findByEntidadAndIdEntidadOrderByFecHoraDesc(entidad, idEntidad)
                .stream().map(AuditoriaResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponseDto> listarPorUsuario(Integer idUsuario) {
        return auditoriaRepository.findByUsuario_IdUsuarioOrderByFecHoraDesc(idUsuario)
                .stream().map(AuditoriaResponseDto::from).toList();
    }

    private Integer actorIdFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Integer id) {
            return id;
        }
        return null;
    }
}
