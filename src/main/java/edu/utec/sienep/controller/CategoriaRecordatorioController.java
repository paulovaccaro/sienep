package edu.utec.sienep.controller;

import edu.utec.sienep.dto.CategoriaRecordatorioCreateDto;
import edu.utec.sienep.dto.CategoriaRecordatorioResponseDto;
import edu.utec.sienep.service.CategoriaRecordatorioService;
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
@RequestMapping("/api/categorias-recordatorio")
@RequiredArgsConstructor
@Tag(name = "Categorías de Recordatorio", description = "Catálogo de categorías; GET público autenticado, CUD requiere recordatorios.gestionar")
public class CategoriaRecordatorioController {

    private final CategoriaRecordatorioService service;
    private final PermisoService permisoService;

    @Operation(summary = "Listar categorías activas")
    @GetMapping
    public ResponseEntity<List<CategoriaRecordatorioResponseDto>> listar() {
        return ResponseEntity.ok(service.listarTodasDto());
    }

    @Operation(summary = "Obtener categoría por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaRecordatorioResponseDto> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerDtoPorId(id));
    }

    @Operation(summary = "Crear categoría de recordatorio")
    @PostMapping
    public ResponseEntity<CategoriaRecordatorioResponseDto> crear(
            @Valid @RequestBody CategoriaRecordatorioCreateDto dto, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "recordatorios.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.crearDto(dto.getNombre(), dto.getDescripcion())
        );
    }

    @Operation(summary = "Actualizar categoría de recordatorio")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaRecordatorioResponseDto> actualizar(
            @PathVariable Integer id,
            @RequestBody CategoriaRecordatorioCreateDto dto, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "recordatorios.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(service.actualizarDto(id, dto.getNombre(), dto.getDescripcion()));
    }

    @Operation(summary = "Desactivar categoría de recordatorio")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "recordatorios.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
