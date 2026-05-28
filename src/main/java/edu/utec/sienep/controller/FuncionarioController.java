package edu.utec.sienep.controller;

import edu.utec.sienep.dto.FuncionarioCreateDto;
import edu.utec.sienep.dto.FuncionarioResponseDto;
import edu.utec.sienep.service.FuncionarioService;
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
@RequestMapping("/api/funcionarios")
@RequiredArgsConstructor
@Tag(name = "Funcionarios", description = "Gestión de funcionarios (permiso global)")
public class FuncionarioController {

    private final FuncionarioService funcionarioService;
    private final PermisoService permisoService;

    @Operation(summary = "Listar todos los funcionarios")
    @GetMapping
    public ResponseEntity<List<FuncionarioResponseDto>> listar(Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "funcionarios.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(funcionarioService.listarTodosDto());
    }

    @Operation(summary = "Obtener funcionario por ID", description = "Propietario o funcionarios.leer global")
    @GetMapping("/{id}")
    public ResponseEntity<FuncionarioResponseDto> obtener(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        boolean esPropietario = userId.equals(id);
        if (!esPropietario && !permisoService.tienePermisoGlobal(userId, "funcionarios.leer"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(funcionarioService.obtenerDtoPorId(id));
    }

    @Operation(summary = "Crear funcionario")
    @PostMapping
    public ResponseEntity<FuncionarioResponseDto> crear(@Valid @RequestBody FuncionarioCreateDto dto,
                                                        Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "funcionarios.crear"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
            funcionarioService.registrarDto(dto.getCedula(), dto.getNombre(), dto.getApellido(),
                    dto.getPassword(), dto.getFecNacimiento())
        );
    }

    @Operation(summary = "Desactivar funcionario")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, Authentication auth) {
        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "funcionarios.eliminar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        funcionarioService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
