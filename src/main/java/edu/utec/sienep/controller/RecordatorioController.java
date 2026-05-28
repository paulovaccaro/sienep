package edu.utec.sienep.controller;

import edu.utec.sienep.dto.InstanciaResponseDto;
import edu.utec.sienep.dto.RecordatorioCreateDto;
import edu.utec.sienep.dto.RecordatorioResponseDto;
import edu.utec.sienep.dto.RecordatorioUpdateDto;
import edu.utec.sienep.service.PermisoService;
import edu.utec.sienep.service.RecordatorioService;
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
@RequestMapping("/api/recordatorios")
@RequiredArgsConstructor
@Tag(name = "Recordatorios", description = "Recordatorios de funcionarios; propietario siempre puede CRUD los suyos")
public class RecordatorioController {

    private final RecordatorioService recordatorioService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar recordatorios", description = "Con recordatorios.leer global devuelve todos; sin él, solo los propios")
    @GetMapping
    public ResponseEntity<List<RecordatorioResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        List<RecordatorioResponseDto> lista = permisoService.tienePermisoGlobal(userId, "recordatorios.leer")
                ? recordatorioService.listarTodosDto()
                : recordatorioService.listarPorFuncionarioDto(userId);
        return ResponseEntity.ok(lista);
    }

    @Operation(summary = "Obtener recordatorio por ID")
    @GetMapping("/{id}")
    public ResponseEntity<RecordatorioResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        RecordatorioResponseDto dto = recordatorioService.obtenerDtoPorId(id);
        boolean esPropietario = dto.getIdFuncionario().equals(userId);
        if (!esPropietario && !permisoService.tienePermisoGlobal(userId, "recordatorios.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Crear recordatorio", description = "idFuncionario se toma del JWT; idEstudiante e idCategoria opcionales")
    @PostMapping
    public ResponseEntity<RecordatorioResponseDto> crear(
            @Valid @RequestBody RecordatorioCreateDto dto, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "recordatorios.crear"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            recordatorioService.crearDto(dto.getTitulo(), dto.getDescripcion(), dto.getFecHora(),
                    dto.getRecurrencia(), userId, dto.getIdEstudiante(), dto.getIdCategoria())
        );
    }

    @Operation(summary = "Actualizar recordatorio")
    @PutMapping("/{id}")
    public ResponseEntity<RecordatorioResponseDto> actualizar(
            @PathVariable Integer id,
            @RequestBody RecordatorioUpdateDto dto, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        RecordatorioResponseDto existing = recordatorioService.obtenerDtoPorId(id);
        boolean esPropietario = existing.getIdFuncionario().equals(userId);
        if (!esPropietario && !permisoService.tienePermisoGlobal(userId, "recordatorios.modificar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(
            recordatorioService.actualizarDto(id, dto.getTitulo(), dto.getDescripcion(),
                    dto.getFecHora(), dto.getRecurrencia(), dto.getIdCategoria(), dto.getEstActivo())
        );
    }

    @Operation(summary = "Desactivar recordatorio")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        RecordatorioResponseDto dto = recordatorioService.obtenerDtoPorId(id);
        boolean esPropietario = dto.getIdFuncionario().equals(userId);
        if (!esPropietario && !permisoService.tienePermisoGlobal(userId, "recordatorios.eliminar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        recordatorioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Convertir recordatorio en instancia", description = "Crea Instancia + Notificación y desactiva el recordatorio (RF27)")
    @PostMapping("/{id}/convertir-instancia")
    public ResponseEntity<InstanciaResponseDto> convertirInstancia(
            @PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        RecordatorioResponseDto dto = recordatorioService.obtenerDtoPorId(id);
        boolean esPropietario = dto.getIdFuncionario().equals(userId);
        if (!esPropietario && !permisoService.tienePermisoGlobal(userId, "recordatorios.modificar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            recordatorioService.convertirInstanciaDto(id)
        );
    }
}
