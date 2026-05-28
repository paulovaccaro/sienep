package edu.utec.sienep.controller;

import edu.utec.sienep.dto.CarreraCreateDto;
import edu.utec.sienep.dto.CarreraResponseDto;
import edu.utec.sienep.service.CarreraService;
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
@RequestMapping("/api/carreras")
@RequiredArgsConstructor
@Tag(name = "Carreras", description = "Catálogo de carreras universitarias")
public class CarreraController {

    private final CarreraService carreraService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar carreras activas")
    @GetMapping
    public ResponseEntity<List<CarreraResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "carreras.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(carreraService.listarActivasDto());
    }

    @Operation(summary = "Obtener carrera por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CarreraResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "carreras.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(carreraService.obtenerDtoPorId(id));
    }

    @Operation(summary = "Crear carrera")
    @PostMapping
    public ResponseEntity<CarreraResponseDto> crear(@Valid @RequestBody CarreraCreateDto dto, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "carreras.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            carreraService.crearDto(dto.getCodigo(), dto.getNombre(), dto.getPlan())
        );
    }

    @Operation(summary = "Actualizar carrera")
    @PutMapping("/{id}")
    public ResponseEntity<CarreraResponseDto> actualizar(@PathVariable Integer id,
                                                         @RequestBody CarreraCreateDto dto,
                                                         Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "carreras.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(
            carreraService.actualizarDto(id, dto.getCodigo(), dto.getNombre(), dto.getPlan(), null)
        );
    }

    @Operation(summary = "Desactivar carrera")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "carreras.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        carreraService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
