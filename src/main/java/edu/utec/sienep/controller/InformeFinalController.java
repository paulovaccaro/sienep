package edu.utec.sienep.controller;

import edu.utec.sienep.dto.InformeFinalCreateDto;
import edu.utec.sienep.dto.InformeFinalResponseDto;
import edu.utec.sienep.service.InformeFinalService;
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
@RequestMapping("/api/informes")
@RequiredArgsConstructor
@Tag(name = "Informes Finales", description = "Informes finales de seguimiento (permiso global)")
public class InformeFinalController {

    private final InformeFinalService informeFinalService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar informes finales")
    @GetMapping
    public ResponseEntity<List<InformeFinalResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "info_final.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(informeFinalService.listarTodosDto());
    }

    @Operation(summary = "Obtener informe final por ID")
    @GetMapping("/{id}")
    public ResponseEntity<InformeFinalResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "info_final.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(informeFinalService.obtenerDtoPorId(id));
    }

    @Operation(summary = "Crear informe final")
    @PostMapping
    public ResponseEntity<InformeFinalResponseDto> crear(@Valid @RequestBody InformeFinalCreateDto dto,
                                                          Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "info_final.crear"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            informeFinalService.crearDto(dto.getContenido(), dto.getValoracion(), dto.getFecCreacion())
        );
    }

    @Operation(summary = "Actualizar informe final")
    @PutMapping("/{id}")
    public ResponseEntity<InformeFinalResponseDto> actualizar(@PathVariable Integer id,
                                                               @RequestBody InformeFinalCreateDto dto,
                                                               Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "info_final.modificar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(
            informeFinalService.actualizarDto(id, dto.getContenido(), dto.getValoracion(), dto.getFecCreacion())
        );
    }

    @Operation(summary = "Desactivar informe final")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "info_final.eliminar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        informeFinalService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
