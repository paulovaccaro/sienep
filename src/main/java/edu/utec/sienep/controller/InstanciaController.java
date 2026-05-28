package edu.utec.sienep.controller;

import edu.utec.sienep.dto.InstanciaClonarDto;
import edu.utec.sienep.dto.InstanciaCreateDto;
import edu.utec.sienep.dto.InstanciaResponseDto;
import edu.utec.sienep.service.InstanciaService;
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
@RequestMapping("/api/instancias")
@RequiredArgsConstructor
@Tag(name = "Instancias", description = "Instancias de eventos/reuniones (permiso global)")
public class InstanciaController {

    private final InstanciaService instanciaService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar instancias")
    @GetMapping
    public ResponseEntity<List<InstanciaResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(instanciaService.listarTodosDto());
    }

    @Operation(summary = "Obtener instancia por ID")
    @GetMapping("/{id}")
    public ResponseEntity<InstanciaResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(instanciaService.obtenerDtoPorId(id));
    }

    @Operation(summary = "Crear instancia")
    @PostMapping
    public ResponseEntity<InstanciaResponseDto> crear(@Valid @RequestBody InstanciaCreateDto dto,
                                                      Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.crear"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            instanciaService.crearDto(dto.getTitulo(), dto.getTipo(), dto.getFecHora(),
                    dto.getDescripcion(), dto.getIdFuncionario(), dto.getIdCategoria())
        );
    }

    @Operation(summary = "Actualizar instancia")
    @PutMapping("/{id}")
    public ResponseEntity<InstanciaResponseDto> actualizar(@PathVariable Integer id,
                                                           @RequestBody InstanciaCreateDto dto,
                                                           Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.modificar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(
            instanciaService.actualizarDto(id, dto.getTitulo(), dto.getTipo(),
                    dto.getFecHora(), dto.getDescripcion(), dto.getIdCategoria())
        );
    }

    @Operation(summary = "Clonar instancia", description = "Copia título/tipo/descripción/funcionario; fecHora en body opcional")
    @PostMapping("/{id}/clonar")
    public ResponseEntity<InstanciaResponseDto> clonar(
            @PathVariable Integer id,
            @RequestBody(required = false) InstanciaClonarDto dto,
            Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.crear"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            instanciaService.clonarDto(id, dto != null ? dto.getFecHora() : null)
        );
    }

    @Operation(summary = "Desactivar instancia")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "instancias.eliminar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        instanciaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
