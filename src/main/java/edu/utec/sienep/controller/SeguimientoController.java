package edu.utec.sienep.controller;

import edu.utec.sienep.dto.SeguimientoCreateDto;
import edu.utec.sienep.dto.SeguimientoResponseDto;
import edu.utec.sienep.service.EstudianteService;
import edu.utec.sienep.service.PermisoService;
import edu.utec.sienep.service.SeguimientoService;
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
@RequestMapping("/api/seguimientos")
@RequiredArgsConstructor
@Tag(name = "Seguimientos", description = "Seguimientos educativos de estudiantes (scope por grupo)")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;
    private final EstudianteService estudianteService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar seguimientos accesibles")
    @GetMapping
    public ResponseEntity<List<SeguimientoResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        List<Integer> grupos = permisoService.gruposAccesibles(userId, "seguimientos.leer");
        if (grupos.isEmpty()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(seguimientoService.listarPorGruposDto(grupos));
    }

    @Operation(summary = "Obtener seguimiento por ID")
    @GetMapping("/{id}")
    public ResponseEntity<SeguimientoResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        SeguimientoResponseDto dto = seguimientoService.buscarDtoPorId(id);
        boolean esPropietario = userId.equals(dto.getIdEstudiante());
        if (!esPropietario && !permisoService.tienePermiso(userId, "seguimientos.leer", dto.getIdGrupo()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Listar seguimientos de un estudiante")
    @GetMapping("/estudiante/{idEstudiante}")
    public ResponseEntity<List<SeguimientoResponseDto>> listarPorEstudiante(@PathVariable Integer idEstudiante,
                                                                             Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        boolean esPropietario = userId.equals(idEstudiante);
        if (!esPropietario) {
            Integer idGrupo = estudianteService.obtenerDtoPorId(idEstudiante).getIdGrupo();
            if (!permisoService.tienePermiso(userId, "seguimientos.leer", idGrupo))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(seguimientoService.listarPorEstudianteDto(idEstudiante));
    }

    @Operation(summary = "Crear seguimiento")
    @PostMapping
    public ResponseEntity<SeguimientoResponseDto> crear(@Valid @RequestBody SeguimientoCreateDto dto,
                                                        Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        Integer idGrupo = estudianteService.obtenerDtoPorId(dto.getIdEstudiante()).getIdGrupo();
        if (!permisoService.tienePermiso(userId, "seguimientos.crear", idGrupo))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            seguimientoService.agregarDto(dto.getIdInforme(), dto.getIdEstudiante(),
                    dto.getFecInicio(), dto.getFecCierre(), dto.isEstActivo())
        );
    }

    @Operation(summary = "Actualizar seguimiento")
    @PutMapping("/{id}")
    public ResponseEntity<SeguimientoResponseDto> actualizar(@PathVariable Integer id,
                                                             @Valid @RequestBody SeguimientoCreateDto dto,
                                                             Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        SeguimientoResponseDto existing = seguimientoService.buscarDtoPorId(id);
        if (!permisoService.tienePermiso(userId, "seguimientos.modificar", existing.getIdGrupo()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(
            seguimientoService.actualizarDto(id, dto.getIdInforme(), dto.getIdEstudiante(),
                    dto.getFecInicio(), dto.getFecCierre(), dto.isEstActivo())
        );
    }

    @Operation(summary = "Eliminar seguimiento")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        SeguimientoResponseDto dto = seguimientoService.buscarDtoPorId(id);
        if (!permisoService.tienePermiso(userId, "seguimientos.eliminar", dto.getIdGrupo()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        seguimientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
