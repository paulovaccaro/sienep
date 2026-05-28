package edu.utec.sienep.controller;

import edu.utec.sienep.dto.GrupoCreateDto;
import edu.utec.sienep.dto.GrupoResponseDto;
import edu.utec.sienep.service.GrupoService;
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
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
@Tag(name = "Grupos", description = "Grupos académicos (carrera + ITR + año + semestre)")
public class GrupoController {

    private final GrupoService grupoService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar grupos activos", description = "Filtra por idItr si se pasa como query param")
    @GetMapping
    public ResponseEntity<List<GrupoResponseDto>> listar(@RequestParam(required = false) Integer idItr,
                                                          Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "grupos.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        List<GrupoResponseDto> result = (idItr != null)
            ? grupoService.listarPorItrDto(idItr)
            : grupoService.listarActivosDto();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Obtener grupo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<GrupoResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "grupos.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(grupoService.obtenerDtoPorId(id));
    }

    @Operation(summary = "Crear grupo")
    @PostMapping
    public ResponseEntity<GrupoResponseDto> crear(@Valid @RequestBody GrupoCreateDto dto, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "grupos.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            grupoService.crearDto(dto.getNomGrupo(), dto.getIdCarrera(), dto.getIdItr(),
                    dto.getAnio(), dto.getSemestre())
        );
    }

    @Operation(summary = "Actualizar grupo")
    @PutMapping("/{id}")
    public ResponseEntity<GrupoResponseDto> actualizar(@PathVariable Integer id,
                                                        @RequestBody GrupoCreateDto dto,
                                                        Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "grupos.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(grupoService.actualizarDto(id, dto.getNomGrupo(), null));
    }

    @Operation(summary = "Desactivar grupo")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "grupos.gestionar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        grupoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
