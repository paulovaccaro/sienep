package edu.utec.sienep.controller;

import edu.utec.sienep.dto.ITRCreateDto;
import edu.utec.sienep.dto.ITRResponseDto;
import edu.utec.sienep.service.ITRService;
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
@RequestMapping("/api/itr")
@RequiredArgsConstructor
@Tag(name = "ITR", description = "Institutos Tecnológicos Regionales (catálogo)")
public class ITRController {

    private final ITRService itrService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar ITRs activos")
    @GetMapping
    public ResponseEntity<List<ITRResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "itr.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(itrService.listarActivosDto());
    }

    @Operation(summary = "Obtener ITR por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ITRResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "itr.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(itrService.obtenerDtoPorId(id));
    }

    @Operation(summary = "Crear ITR")
    @PostMapping
    public ResponseEntity<ITRResponseDto> crear(@Valid @RequestBody ITRCreateDto dto, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "itr.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            itrService.crearDto(dto.getCodigo(), dto.getNombre(), dto.getIdDireccion())
        );
    }

    @Operation(summary = "Actualizar ITR")
    @PutMapping("/{id}")
    public ResponseEntity<ITRResponseDto> actualizar(@PathVariable Integer id,
                                                     @RequestBody ITRCreateDto dto,
                                                     Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "itr.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(
            itrService.actualizarDto(id, dto.getCodigo(), dto.getNombre(), null)
        );
    }

    @Operation(summary = "Desactivar ITR")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "itr.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        itrService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
