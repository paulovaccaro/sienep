package edu.utec.sienep.controller;

import edu.utec.sienep.dto.EstudianteCreateDto;
import edu.utec.sienep.dto.EstudianteResponseDto;
import edu.utec.sienep.dto.EstudianteUpdateDto;
import edu.utec.sienep.service.EstudianteService;
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
@RequestMapping("/api/estudiantes")
@RequiredArgsConstructor
@Tag(name = "Estudiantes", description = "Gestión de estudiantes (scope por grupo)")
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar estudiantes accesibles", description = "Devuelve los estudiantes de los grupos accesibles para el usuario autenticado")
    @GetMapping
    public ResponseEntity<List<EstudianteResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        List<Integer> grupos = permisoService.gruposAccesibles(userId, "estudiantes.leer");
        if (grupos.isEmpty()) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(estudianteService.listarPorGruposDto(grupos));
    }

    @Operation(summary = "Obtener estudiante por ID")
    @GetMapping("/{id}")
    public ResponseEntity<EstudianteResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        EstudianteResponseDto dto = estudianteService.obtenerDtoPorId(id);
        boolean esPropietario = userId.equals(id);
        List<Integer> grupos = permisoService.gruposAccesibles(userId, "estudiantes.leer");
        if (!esPropietario && !grupos.contains(dto.getIdGrupo()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Crear estudiante", description = "Permiso: estudiantes.crear en el grupo del estudiante")
    @PostMapping
    public ResponseEntity<EstudianteResponseDto> crear(@Valid @RequestBody EstudianteCreateDto dto,
                                                       Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermiso(userId, "estudiantes.crear", dto.getIdGrupo()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            estudianteService.registrarDto(dto.getCedula(), dto.getNombre(), dto.getApellido(),
                    dto.getPassword(), dto.getFecNacimiento(), dto.getIdGrupo())
        );
    }

    @Operation(summary = "Actualizar estudiante")
    @PutMapping("/{id}")
    public ResponseEntity<EstudianteResponseDto> actualizar(@PathVariable Integer id,
                                                            @RequestBody EstudianteUpdateDto dto,
                                                            Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        EstudianteResponseDto existing = estudianteService.obtenerDtoPorId(id);
        boolean esPropietario = userId.equals(id);
        if (!esPropietario && !permisoService.tienePermiso(userId, "estudiantes.modificar", existing.getIdGrupo()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(
            estudianteService.actualizarDto(id, dto.getNombre(), dto.getApellido(),
                    dto.getIdGrupo(), dto.getEstActivo())
        );
    }

    @Operation(summary = "Desactivar estudiante")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        EstudianteResponseDto dto = estudianteService.obtenerDtoPorId(id);
        if (!permisoService.tienePermiso(userId, "estudiantes.eliminar", dto.getIdGrupo()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        estudianteService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
