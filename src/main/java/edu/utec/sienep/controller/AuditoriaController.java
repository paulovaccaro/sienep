package edu.utec.sienep.controller;

import edu.utec.sienep.dto.AuditoriaResponseDto;
import edu.utec.sienep.service.AuditoriaService;
import edu.utec.sienep.service.PermisoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Consulta de trazabilidad y auditoría del sistema (permiso auditoria.leer)")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar todos los eventos de auditoría", description = "Devuelve todos los registros ordenados por fecha descendente")
    @GetMapping
    public ResponseEntity<List<AuditoriaResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "auditoria.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(auditoriaService.listarTodos());
    }

    @Operation(summary = "Filtrar por entidad e ID de entidad")
    @GetMapping("/entidad/{entidad}/{idEntidad}")
    public ResponseEntity<List<AuditoriaResponseDto>> listarPorEntidad(
            @PathVariable String entidad,
            @PathVariable String idEntidad,
            Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "auditoria.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(auditoriaService.listarPorEntidad(entidad, idEntidad));
    }

    @Operation(summary = "Filtrar por usuario actor")
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<AuditoriaResponseDto>> listarPorUsuario(
            @PathVariable Integer idUsuario,
            Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "auditoria.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(auditoriaService.listarPorUsuario(idUsuario));
    }
}
