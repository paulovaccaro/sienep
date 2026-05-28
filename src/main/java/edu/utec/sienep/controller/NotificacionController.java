package edu.utec.sienep.controller;

import edu.utec.sienep.dto.NotificacionCreateDto;
import edu.utec.sienep.dto.NotificacionResponseDto;
import edu.utec.sienep.service.NotificacionService;
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
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Notificaciones asociadas a instancias (permiso global)")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar todas las notificaciones")
    @GetMapping
    public ResponseEntity<List<NotificacionResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "notificaciones.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(notificacionService.listarTodasDto());
    }

    @Operation(summary = "Listar notificaciones de una instancia")
    @GetMapping("/instancia/{idInstancia}")
    public ResponseEntity<List<NotificacionResponseDto>> listarPorInstancia(@PathVariable Integer idInstancia,
                                                                              Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "notificaciones.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(notificacionService.listarPorInstanciaDto(idInstancia));
    }

    @Operation(summary = "Obtener notificación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<NotificacionResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "notificaciones.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(notificacionService.obtenerDtoPorId(id));
    }

    @Operation(summary = "Crear notificación", description = "Requiere una Instancia existente (FK NOT NULL)")
    @PostMapping
    public ResponseEntity<NotificacionResponseDto> crear(@Valid @RequestBody NotificacionCreateDto dto,
                                                          Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "notificaciones.crear"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            notificacionService.crearDto(dto.getIdInstancia(), dto.getAsunto(),
                    dto.getMensaje(), dto.getDestinatario())
        );
    }

    @Operation(summary = "Desactivar notificación")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "notificaciones.eliminar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        notificacionService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
