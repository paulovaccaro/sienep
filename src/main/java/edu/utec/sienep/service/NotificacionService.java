package edu.utec.sienep.service;

import edu.utec.sienep.dto.NotificacionResponseDto;
import edu.utec.sienep.entity.Instancia;
import edu.utec.sienep.entity.Notificacion;
import edu.utec.sienep.repository.InstanciaRepository;
import edu.utec.sienep.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final InstanciaRepository instanciaRepository;

    @Transactional(readOnly = true)
    public List<Notificacion> listarPorInstancia(Integer idInstancia) {
        return notificacionRepository.findByInstancia_IdInstancia(idInstancia);
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listarTodas() {
        return notificacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Notificacion obtenerPorId(Integer id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada: " + id));
    }

    @Transactional
    public Notificacion crear(Integer idInstancia, String asunto, String mensaje, String destinatario) {
        Instancia instancia = instanciaRepository.findById(idInstancia)
                .orElseThrow(() -> new IllegalArgumentException("Instancia no encontrada: " + idInstancia));

        Notificacion notif = new Notificacion();
        notif.setInstancia(instancia);
        notif.setAsunto(asunto);
        notif.setMensaje(mensaje);
        notif.setDestinatario(destinatario);
        notif.setFecEnvio(LocalDate.now());
        notif.setEstActivo(true);
        return notificacionRepository.save(notif);
    }

    @Transactional
    public void desactivar(Integer id) {
        Notificacion notif = obtenerPorId(id);
        notif.setEstActivo(false);
        notificacionRepository.save(notif);
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponseDto> listarTodasDto() {
        return listarTodas().stream().map(NotificacionResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponseDto> listarPorInstanciaDto(Integer idInstancia) {
        return listarPorInstancia(idInstancia).stream().map(NotificacionResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public NotificacionResponseDto obtenerDtoPorId(Integer id) {
        return NotificacionResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public NotificacionResponseDto crearDto(Integer idInstancia, String asunto,
                                             String mensaje, String destinatario) {
        return NotificacionResponseDto.from(crear(idInstancia, asunto, mensaje, destinatario));
    }
}
