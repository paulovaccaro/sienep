package edu.utec.sienep.controller;

import edu.utec.sienep.dto.IncidenciaCreateDto;
import edu.utec.sienep.dto.IncidenciaResponseDto;
import edu.utec.sienep.service.IncidenciaService;
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
@RequestMapping("/api/incidencias")
@RequiredArgsConstructor
@Tag(name = "Incidencias", description = "Incidencias asociadas a instancias; DELETE desactiva la instancia subyacente")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar incidencias")
    @GetMapping
    public ResponseEntity<List<IncidenciaResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "incidencias.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(incidenciaService.listarTodosDto());
    }

    @Operation(summary = "Obtener incidencia por ID")
    @GetMapping("/{id}")
    public ResponseEntity<IncidenciaResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "incidencias.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(incidenciaService.obtenerDtoPorId(id));
    }

    @Operation(summary = "Listar incidencias de un funcionario", description = "Propietario o incidencias.leer global")
    @GetMapping("/funcionario/{idFuncionario}")
    public ResponseEntity<List<IncidenciaResponseDto>> listarPorFuncionario(@PathVariable Integer idFuncionario,
                                                                              Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        boolean esPropietario = userId.equals(idFuncionario);
        if (!esPropietario && !permisoService.tienePermisoGlobal(userId, "incidencias.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(incidenciaService.listarPorFuncionarioDto(idFuncionario));
    }

    @Operation(summary = "Crear incidencia", description = "Requiere una Instancia preexistente; guarda el lugar del hecho")
    @PostMapping
    public ResponseEntity<IncidenciaResponseDto> crear(@Valid @RequestBody IncidenciaCreateDto dto,
                                                        Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "incidencias.crear"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            incidenciaService.crearDto(dto.getIdInstancia(), dto.getLugar())
        );
    }

    @Operation(summary = "Actualizar incidencia")
    @PutMapping("/{id}")
    public ResponseEntity<IncidenciaResponseDto> actualizar(@PathVariable Integer id,
                                                             @RequestBody IncidenciaCreateDto dto,
                                                             Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "incidencias.modificar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(incidenciaService.actualizarDto(id, dto.getLugar()));
    }

    @Operation(summary = "Desactivar incidencia", description = "Desactiva también la Instancia subyacente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "incidencias.eliminar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        incidenciaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
