package edu.utec.sienep.controller;

import edu.utec.sienep.dto.ObservacionCreateDto;
import edu.utec.sienep.dto.ObservacionResponseDto;
import edu.utec.sienep.service.EstudianteService;
import edu.utec.sienep.service.ObservacionService;
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
@RequestMapping("/api/observaciones")
@RequiredArgsConstructor
@Tag(name = "Observaciones", description = "Observaciones de funcionarios sobre estudiantes (scope por grupo)")
public class ObservacionController {

    private final ObservacionService observacionService;
    private final EstudianteService estudianteService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar observaciones de un estudiante")
    @GetMapping("/estudiante/{idEstudiante}")
    public ResponseEntity<List<ObservacionResponseDto>> listarPorEstudiante(@PathVariable Integer idEstudiante,
                                                                              Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        boolean esPropietario = userId.equals(idEstudiante);
        if (!esPropietario) {
            Integer idGrupo = estudianteService.obtenerDtoPorId(idEstudiante).getIdGrupo();
            if (!permisoService.tienePermiso(userId, "observaciones.leer", idGrupo))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(observacionService.listarPorEstudianteDto(idEstudiante));
    }

    @Operation(summary = "Obtener observación por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ObservacionResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        ObservacionResponseDto dto = observacionService.obtenerDtoPorId(id);
        if (!permisoService.tienePermiso(userId, "observaciones.leer", dto.getIdGrupo()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Crear observación", description = "El funcionario autenticado queda como autor (idFuncionario del JWT)")
    @PostMapping
    public ResponseEntity<ObservacionResponseDto> crear(@Valid @RequestBody ObservacionCreateDto dto,
                                                         Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        Integer idGrupo = estudianteService.obtenerDtoPorId(dto.getIdEstudiante()).getIdGrupo();
        if (!permisoService.tienePermiso(userId, "observaciones.crear", idGrupo))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            observacionService.crearDto(userId, dto.getIdEstudiante(), dto.getTitulo(), dto.getContenido())
        );
    }

    @Operation(summary = "Desactivar observación")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        ObservacionResponseDto dto = observacionService.obtenerDtoPorId(id);
        if (!permisoService.tienePermiso(userId, "observaciones.eliminar", dto.getIdGrupo()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        observacionService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
