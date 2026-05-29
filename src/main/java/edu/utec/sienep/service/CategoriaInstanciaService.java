package edu.utec.sienep.service;

import edu.utec.sienep.dto.CategoriaInstanciaResponseDto;
import edu.utec.sienep.entity.CategoriaInstancia;
import edu.utec.sienep.repository.CategoriaInstanciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaInstanciaService {

    private final CategoriaInstanciaRepository repo;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<CategoriaInstancia> listarTodas() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public CategoriaInstancia obtenerPorId(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría de instancia no encontrada: " + id));
    }

    @Transactional
    public CategoriaInstancia crear(String nombre, String descripcion) {
        CategoriaInstancia c = new CategoriaInstancia();
        c.setNombre(nombre);
        c.setDescripcion(descripcion);
        c.setEstActivo(true);
        CategoriaInstancia saved = repo.save(c);
        auditoriaService.registrar("CREAR", "categorias_instancia", String.valueOf(saved.getIdCategoriaInstancia()), nombre);
        return saved;
    }

    @Transactional
    public CategoriaInstancia actualizar(Integer id, String nombre, String descripcion) {
        CategoriaInstancia c = obtenerPorId(id);
        if (nombre != null) c.setNombre(nombre);
        if (descripcion != null) c.setDescripcion(descripcion);
        CategoriaInstancia saved = repo.save(c);
        auditoriaService.registrar("MODIFICAR", "categorias_instancia", String.valueOf(id), null);
        return saved;
    }

    @Transactional
    public void desactivar(Integer id) {
        CategoriaInstancia c = obtenerPorId(id);
        c.setEstActivo(false);
        repo.save(c);
        auditoriaService.registrar("BAJA", "categorias_instancia", String.valueOf(id), null);
    }

    @Transactional(readOnly = true)
    public List<CategoriaInstanciaResponseDto> listarTodasDto() {
        return listarTodas().stream().map(CategoriaInstanciaResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaInstanciaResponseDto obtenerDtoPorId(Integer id) {
        return CategoriaInstanciaResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public CategoriaInstanciaResponseDto crearDto(String nombre, String descripcion) {
        return CategoriaInstanciaResponseDto.from(crear(nombre, descripcion));
    }

    @Transactional
    public CategoriaInstanciaResponseDto actualizarDto(Integer id, String nombre, String descripcion) {
        return CategoriaInstanciaResponseDto.from(actualizar(id, nombre, descripcion));
    }
}
