package edu.utec.sienep.controller;

import edu.utec.sienep.dto.RolCreateDto;
import edu.utec.sienep.dto.RolResponseDto;
import edu.utec.sienep.service.PermisoService;
import edu.utec.sienep.service.RolService;
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
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Gestión de roles del sistema (RF32-RF34); requiere permiso roles.gestionar")
public class RolController {

    private final RolService rolService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar todos los roles (RF33)", description = "Incluye roles de sistema y personalizados")
    @GetMapping
    public ResponseEntity<List<RolResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "roles.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(rolService.listarTodosDto());
    }

    @Operation(summary = "Obtener rol por ID")
    @GetMapping("/{id}")
    public ResponseEntity<RolResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "roles.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(rolService.obtenerDtoPorId(id));
    }

    @Operation(summary = "Crear rol personalizado (RF34)", description = "Crea un nuevo rol no-sistema asignable a usuarios")
    @PostMapping
    public ResponseEntity<RolResponseDto> crear(@Valid @RequestBody RolCreateDto dto, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "roles.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            rolService.crearDto(dto.getNombre(), dto.getDescripcion())
        );
    }

    @Operation(summary = "Actualizar rol (RF32)")
    @PutMapping("/{id}")
    public ResponseEntity<RolResponseDto> actualizar(@PathVariable Integer id,
                                                      @RequestBody RolCreateDto dto,
                                                      Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "roles.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(
            rolService.actualizarDto(id, dto.getNombre(), dto.getDescripcion(), null)
        );
    }

    @Operation(summary = "Desactivar rol personalizado (RF32)", description = "No permite desactivar roles de sistema (esSistema=true)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "roles.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        rolService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
