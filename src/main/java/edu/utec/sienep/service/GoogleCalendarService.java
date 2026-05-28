package edu.utec.sienep.service;

import edu.utec.sienep.dto.EventoCalendarioResponseDto;
import edu.utec.sienep.entity.EventoCalendario;
import edu.utec.sienep.entity.Instancia;
import edu.utec.sienep.entity.Recordatorio;
import edu.utec.sienep.repository.EventoCalendarioRepository;
import edu.utec.sienep.repository.InstanciaRepository;
import edu.utec.sienep.repository.RecordatorioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Simulación de integración con Google Calendar (RF13, RF22).
 * Persiste los eventos en la tabla eventos_calendario y genera
 * IDs y links con el mismo formato que la API real de Google.
 * Para activar la integración real, reemplazar los métodos privados
 * por llamadas al SDK de Google Calendar con credenciales OAuth2.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private static final DateTimeFormatter GCF = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private final EventoCalendarioRepository eventoRepo;
    private final InstanciaRepository instanciaRepository;
    private final RecordatorioRepository recordatorioRepository;

    // ----------------------------------------------------------------
    // Creación automática desde otras entidades
    // ----------------------------------------------------------------

    @Transactional
    public EventoCalendario crearDesdeInstancia(Instancia instancia) {
        OffsetDateTime fin = instancia.getFecHora().plusHours(1);
        String eventId = "SIENEP-INST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String link = buildCalendarLink(instancia.getTitulo(), instancia.getFecHora(), fin,
                instancia.getDescripcion(), null);

        EventoCalendario evento = new EventoCalendario();
        evento.setTitulo(instancia.getTitulo());
        evento.setDescripcion(instancia.getDescripcion());
        evento.setFecInicio(instancia.getFecHora());
        evento.setFecFin(fin);
        evento.setInstancia(instancia);
        evento.setGoogleEventId(eventId);
        evento.setGoogleCalendarLink(link);
        evento.setSincronizado(false);
        evento.setEstActivo(true);

        EventoCalendario saved = eventoRepo.save(evento);
        log.info("[Google Calendar SIMULADO] Evento creado para instancia #{}: id={}", instancia.getIdInstancia(), eventId);
        return saved;
    }

    @Transactional
    public EventoCalendario crearDesdeRecordatorio(Recordatorio recordatorio) {
        OffsetDateTime fin = recordatorio.getFecHora().plusHours(1);
        String eventId = "SIENEP-REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String link = buildCalendarLink(recordatorio.getTitulo(), recordatorio.getFecHora(), fin,
                recordatorio.getDescripcion(), null);

        EventoCalendario evento = new EventoCalendario();
        evento.setTitulo(recordatorio.getTitulo());
        evento.setDescripcion(recordatorio.getDescripcion());
        evento.setFecInicio(recordatorio.getFecHora());
        evento.setFecFin(fin);
        evento.setRecordatorio(recordatorio);
        evento.setGoogleEventId(eventId);
        evento.setGoogleCalendarLink(link);
        evento.setSincronizado(false);
        evento.setEstActivo(true);

        EventoCalendario saved = eventoRepo.save(evento);
        log.info("[Google Calendar SIMULADO] Evento creado para recordatorio #{}: id={}", recordatorio.getIdRecordatorio(), eventId);
        return saved;
    }

    // ----------------------------------------------------------------
    // CRUD
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<EventoCalendario> listarTodos() {
        return eventoRepo.findAll();
    }

    @Transactional(readOnly = true)
    public List<EventoCalendario> listarPendientesSincronizacion() {
        return eventoRepo.findBySincronizado(false);
    }

    @Transactional(readOnly = true)
    public List<EventoCalendario> listarPorInstancia(Integer idInstancia) {
        return eventoRepo.findByInstancia_IdInstancia(idInstancia);
    }

    @Transactional(readOnly = true)
    public List<EventoCalendario> listarPorRecordatorio(Integer idRecordatorio) {
        return eventoRepo.findByRecordatorio_IdRecordatorio(idRecordatorio);
    }

    @Transactional(readOnly = true)
    public EventoCalendario obtenerPorId(Integer id) {
        return eventoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento de calendario no encontrado: " + id));
    }

    @Transactional
    public EventoCalendario crear(String titulo, String descripcion, OffsetDateTime fecInicio,
                                  OffsetDateTime fecFin, String ubicacion,
                                  Integer idInstancia, Integer idRecordatorio) {
        OffsetDateTime fin = fecFin != null ? fecFin : fecInicio.plusHours(1);
        String eventId = "SIENEP-MAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String link = buildCalendarLink(titulo, fecInicio, fin, descripcion, ubicacion);

        EventoCalendario evento = new EventoCalendario();
        evento.setTitulo(titulo);
        evento.setDescripcion(descripcion);
        evento.setFecInicio(fecInicio);
        evento.setFecFin(fin);
        evento.setUbicacion(ubicacion);
        evento.setGoogleEventId(eventId);
        evento.setGoogleCalendarLink(link);
        evento.setSincronizado(false);
        evento.setEstActivo(true);

        if (idInstancia != null)
            evento.setInstancia(instanciaRepository.findById(idInstancia)
                    .orElseThrow(() -> new IllegalArgumentException("Instancia no encontrada: " + idInstancia)));
        if (idRecordatorio != null)
            evento.setRecordatorio(recordatorioRepository.findById(idRecordatorio)
                    .orElseThrow(() -> new IllegalArgumentException("Recordatorio no encontrado: " + idRecordatorio)));

        return eventoRepo.save(evento);
    }

    @Transactional
    public EventoCalendario actualizar(Integer id, String titulo, String descripcion,
                                       OffsetDateTime fecInicio, OffsetDateTime fecFin,
                                       String ubicacion) {
        EventoCalendario evento = obtenerPorId(id);
        if (titulo != null)      evento.setTitulo(titulo);
        if (descripcion != null) evento.setDescripcion(descripcion);
        if (fecInicio != null)   evento.setFecInicio(fecInicio);
        if (fecFin != null)      evento.setFecFin(fecFin);
        if (ubicacion != null)   evento.setUbicacion(ubicacion);

        // Regenerar link con los datos actualizados
        OffsetDateTime inicio = evento.getFecInicio();
        OffsetDateTime fin    = evento.getFecFin() != null ? evento.getFecFin() : inicio.plusHours(1);
        evento.setGoogleCalendarLink(buildCalendarLink(evento.getTitulo(), inicio, fin,
                evento.getDescripcion(), evento.getUbicacion()));

        // Si ya estaba sincronizado, marcarlo como pendiente de re-sync
        if (Boolean.TRUE.equals(evento.getSincronizado())) {
            evento.setSincronizado(false);
            log.info("[Google Calendar SIMULADO] Evento {} modificado, marcado para re-sincronización.", evento.getGoogleEventId());
        }

        return eventoRepo.save(evento);
    }

    /** RF13 / RF22: simula el envío del evento a Google Calendar */
    @Transactional
    public EventoCalendario sincronizar(Integer id) {
        EventoCalendario evento = obtenerPorId(id);
        evento.setSincronizado(true);
        eventoRepo.save(evento);
        log.info("[Google Calendar SIMULADO] PUT https://www.googleapis.com/calendar/v3/calendars/primary/events/{} -> 200 OK", evento.getGoogleEventId());
        return evento;
    }

    @Transactional
    public void desactivar(Integer id) {
        EventoCalendario evento = obtenerPorId(id);
        evento.setEstActivo(false);
        eventoRepo.save(evento);
        log.info("[Google Calendar SIMULADO] DELETE event {} -> 204 No Content", evento.getGoogleEventId());
    }

    // ----------------------------------------------------------------
    // DTO-returning methods for controllers
    // ----------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<EventoCalendarioResponseDto> listarTodosDto() {
        return listarTodos().stream().map(EventoCalendarioResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EventoCalendarioResponseDto> listarPendientesSincronizacionDto() {
        return listarPendientesSincronizacion().stream().map(EventoCalendarioResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EventoCalendarioResponseDto> listarPorInstanciaDto(Integer idInstancia) {
        return listarPorInstancia(idInstancia).stream().map(EventoCalendarioResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<EventoCalendarioResponseDto> listarPorRecordatorioDto(Integer idRecordatorio) {
        return listarPorRecordatorio(idRecordatorio).stream().map(EventoCalendarioResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public EventoCalendarioResponseDto obtenerDtoPorId(Integer id) {
        return EventoCalendarioResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public EventoCalendarioResponseDto crearDto(String titulo, String descripcion, OffsetDateTime fecInicio,
                                                 OffsetDateTime fecFin, String ubicacion,
                                                 Integer idInstancia, Integer idRecordatorio) {
        return EventoCalendarioResponseDto.from(crear(titulo, descripcion, fecInicio, fecFin,
                ubicacion, idInstancia, idRecordatorio));
    }

    @Transactional
    public EventoCalendarioResponseDto actualizarDto(Integer id, String titulo, String descripcion,
                                                      OffsetDateTime fecInicio, OffsetDateTime fecFin,
                                                      String ubicacion) {
        return EventoCalendarioResponseDto.from(actualizar(id, titulo, descripcion, fecInicio, fecFin, ubicacion));
    }

    @Transactional
    public EventoCalendarioResponseDto sincronizarDto(Integer id) {
        return EventoCalendarioResponseDto.from(sincronizar(id));
    }

    // ----------------------------------------------------------------
    // Privados
    // ----------------------------------------------------------------

    private String buildCalendarLink(String titulo, OffsetDateTime inicio, OffsetDateTime fin,
                                     String descripcion, String ubicacion) {
        String t = encode(titulo != null ? titulo : "");
        String d = encode(descripcion != null ? descripcion : "");
        String l = encode(ubicacion != null ? ubicacion : "");
        String dates = inicio.format(GCF) + "/" + fin.format(GCF);
        return "https://calendar.google.com/calendar/r/eventedit"
                + "?text=" + t
                + "&dates=" + dates
                + "&details=" + d
                + (ubicacion != null ? "&location=" + l : "");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
