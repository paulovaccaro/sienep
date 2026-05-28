package edu.utec.sienep.controller;

import edu.utec.sienep.dto.AsignacionCreateDto;
import edu.utec.sienep.dto.AsignacionResponseDto;
import edu.utec.sienep.dto.AsignacionUpdateDto;
import edu.utec.sienep.service.AsignacionService;
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
@RequestMapping("/api/asignaciones")
@RequiredArgsConstructor
@Tag(name = "Asignaciones / RBAC", description = "Gestión de asignaciones de rol con scope (itr/carrera/grupo)")
public class AsignacionController {

    private final AsignacionService asignacionService;
    private final PermisoService permisoService;

    @Operation(summary = "Crear asignación de rol")
    @PostMapping
    public ResponseEntity<AsignacionResponseDto> crear(@Valid @RequestBody AsignacionCreateDto dto,
                                                        Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "asignaciones.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            asignacionService.crearDto(dto.getIdUsuario(), dto.getIdRol(),
                    dto.getIdItr(), dto.getIdCarrera(), dto.getIdGrupo())
        );
    }

    @Operation(summary = "Listar asignaciones de un usuario", description = "Propietario o asignaciones.gestionar global")
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<AsignacionResponseDto>> listarPorUsuario(@PathVariable Integer idUsuario,
                                                                          Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        boolean esPropietario = userId.equals(idUsuario);
        if (!esPropietario && !permisoService.tienePermisoGlobal(userId, "asignaciones.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(asignacionService.listarPorUsuarioDto(idUsuario));
    }

    @Operation(summary = "Obtener asignación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<AsignacionResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "asignaciones.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(asignacionService.obtenerDtoPorId(id));
    }

    @Operation(summary = "Actualizar asignación", description = "Usa limpiarItr/limpiarCarrera/limpiarGrupo=true para quitar scope")
    @PutMapping("/{id}")
    public ResponseEntity<AsignacionResponseDto> actualizar(@PathVariable Integer id,
                                                             @RequestBody AsignacionUpdateDto dto,
                                                             Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "asignaciones.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(
            asignacionService.actualizarDto(id,
                    dto.getIdRol(),
                    dto.getIdItr(), dto.getLimpiarItr(),
                    dto.getIdCarrera(), dto.getLimpiarCarrera(),
                    dto.getIdGrupo(), dto.getLimpiarGrupo(),
                    dto.getEstActivo())
        );
    }

    @Operation(summary = "Desactivar asignación")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "asignaciones.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        asignacionService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
