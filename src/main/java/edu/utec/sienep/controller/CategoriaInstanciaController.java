package edu.utec.sienep.controller;

import edu.utec.sienep.dto.CategoriaInstanciaCreateDto;
import edu.utec.sienep.dto.CategoriaInstanciaResponseDto;
import edu.utec.sienep.service.CategoriaInstanciaService;
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
@RequestMapping("/api/categorias-instancia")
@RequiredArgsConstructor
@Tag(name = "Categorías de Instancia", description = "Catálogo de categorías de instancias (RF35-RF37); GET público autenticado, CUD requiere instancias.gestionar")
public class CategoriaInstanciaController {

    private final CategoriaInstanciaService service;
    private final PermisoService permisoService;

    @Operation(summary = "Listar categorías activas")
    @GetMapping
    public ResponseEntity<List<CategoriaInstanciaResponseDto>> listar() {
        return ResponseEntity.ok(service.listarTodasDto());
    }

    @Operation(summary = "Obtener categoría por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaInstanciaResponseDto> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(service.obtenerDtoPorId(id));
    }

    @Operation(summary = "Crear categoría de instancia (RF35)")
    @PostMapping
    public ResponseEntity<CategoriaInstanciaResponseDto> crear(
            @Valid @RequestBody CategoriaInstanciaCreateDto dto, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            service.crearDto(dto.getNombre(), dto.getDescripcion())
        );
    }

    @Operation(summary = "Actualizar categoría de instancia (RF37)")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaInstanciaResponseDto> actualizar(
            @PathVariable Integer id,
            @RequestBody CategoriaInstanciaCreateDto dto, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(service.actualizarDto(id, dto.getNombre(), dto.getDescripcion()));
    }

    @Operation(summary = "Desactivar categoría de instancia (RF36)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        service.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
