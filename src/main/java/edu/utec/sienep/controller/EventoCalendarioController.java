package edu.utec.sienep.controller;

import edu.utec.sienep.dto.EventoCalendarioCreateDto;
import edu.utec.sienep.dto.EventoCalendarioResponseDto;
import edu.utec.sienep.service.GoogleCalendarService;
import edu.utec.sienep.service.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos-calendario")
@RequiredArgsConstructor
@Tag(name = "Google Calendar (simulación)", description = "RF13 / RF22 — Eventos de calendario generados automáticamente al crear instancias y recordatorios")
public class EventoCalendarioController {

    private final GoogleCalendarService googleCalendarService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar todos los eventos")
    @GetMapping
    public ResponseEntity<List<EventoCalendarioResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(googleCalendarService.listarTodosDto());
    }

    @Operation(summary = "Listar eventos pendientes de sincronización")
    @GetMapping("/pendientes")
    public ResponseEntity<List<EventoCalendarioResponseDto>> listarPendientes(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(googleCalendarService.listarPendientesSincronizacionDto());
    }

    @Operation(summary = "Obtener evento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<EventoCalendarioResponseDto> obtener(@PathVariable Integer id,
                                                               Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(googleCalendarService.obtenerDtoPorId(id));
    }

    @Operation(summary = "Listar eventos de una instancia")
    @GetMapping("/instancia/{idInstancia}")
    public ResponseEntity<List<EventoCalendarioResponseDto>> listarPorInstancia(
            @PathVariable Integer idInstancia, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(googleCalendarService.listarPorInstanciaDto(idInstancia));
    }

    @Operation(summary = "Listar eventos de un recordatorio")
    @GetMapping("/recordatorio/{idRecordatorio}")
    public ResponseEntity<List<EventoCalendarioResponseDto>> listarPorRecordatorio(
            @PathVariable Integer idRecordatorio, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(googleCalendarService.listarPorRecordatorioDto(idRecordatorio));
    }

    @Operation(summary = "Crear evento manualmente")
    @PostMapping
    public ResponseEntity<EventoCalendarioResponseDto> crear(
            @Valid @RequestBody EventoCalendarioCreateDto dto, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.crear"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            googleCalendarService.crearDto(dto.getTitulo(), dto.getDescripcion(),
                    dto.getFecInicio(), dto.getFecFin(), dto.getUbicacion(),
                    dto.getIdInstancia(), dto.getIdRecordatorio())
        );
    }

    @Operation(summary = "Actualizar evento")
    @PutMapping("/{id}")
    public ResponseEntity<EventoCalendarioResponseDto> actualizar(
            @PathVariable Integer id,
            @RequestBody EventoCalendarioCreateDto dto, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.modificar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(
            googleCalendarService.actualizarDto(id, dto.getTitulo(), dto.getDescripcion(),
                    dto.getFecInicio(), dto.getFecFin(), dto.getUbicacion())
        );
    }

    @Operation(
        summary = "Sincronizar evento con Google Calendar (RF13 / RF22)",
        description = "Simula el envío del evento a la API de Google Calendar y marca el evento como sincronizado"
    )
    @PostMapping("/{id}/sincronizar")
    public ResponseEntity<EventoCalendarioResponseDto> sincronizar(
            @PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.modificar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(googleCalendarService.sincronizarDto(id));
    }

    @Operation(summary = "Eliminar evento (baja lógica)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.eliminar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        googleCalendarService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
