package edu.utec.sienep.controller;

import edu.utec.sienep.dto.ArchivoAdjuntoCreateDto;
import edu.utec.sienep.dto.ArchivoAdjuntoResponseDto;
import edu.utec.sienep.service.ArchivoAdjuntoService;
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
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
@Tag(name = "Archivos Adjuntos", description = "Archivos adjuntos de estudiantes (scope por grupo)")
public class ArchivoAdjuntoController {

    private final ArchivoAdjuntoService archivoService;
    private final EstudianteService estudianteService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar archivos de un estudiante")
    @GetMapping("/estudiante/{idEstudiante}")
    public ResponseEntity<List<ArchivoAdjuntoResponseDto>> listarPorEstudiante(@PathVariable Integer idEstudiante,
                                                                                 Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        boolean esPropietario = userId.equals(idEstudiante);
        if (!esPropietario) {
            Integer idGrupo = estudianteService.obtenerDtoPorId(idEstudiante).getIdGrupo();
            if (!permisoService.tienePermiso(userId, "arch_adjuntos.leer", idGrupo))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(archivoService.listarPorEstudianteDto(idEstudiante));
    }

    @Operation(summary = "Obtener archivo adjunto por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ArchivoAdjuntoResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        ArchivoAdjuntoResponseDto dto = archivoService.obtenerDtoPorId(id);
        if (!permisoService.tienePermiso(userId, "arch_adjuntos.leer", dto.getIdGrupo()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Registrar archivo adjunto", description = "El funcionario autenticado queda como subidor")
    @PostMapping
    public ResponseEntity<ArchivoAdjuntoResponseDto> crear(@Valid @RequestBody ArchivoAdjuntoCreateDto dto,
                                                             Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        Integer idGrupo = estudianteService.obtenerDtoPorId(dto.getIdEstudiante()).getIdGrupo();
        if (!permisoService.tienePermiso(userId, "arch_adjuntos.crear", idGrupo))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            archivoService.crearDto(userId, dto.getIdEstudiante(), dto.getRuta(), dto.getCategoria())
        );
    }

    @Operation(summary = "Desactivar archivo adjunto")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        ArchivoAdjuntoResponseDto dto = archivoService.obtenerDtoPorId(id);
        if (!permisoService.tienePermiso(userId, "arch_adjuntos.eliminar", dto.getIdGrupo()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        archivoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
