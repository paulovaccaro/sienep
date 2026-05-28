package edu.utec.sienep.service;

import edu.utec.sienep.dto.CategoriaRecordatorioResponseDto;
import edu.utec.sienep.entity.CategoriaRecordatorio;
import edu.utec.sienep.repository.CategoriaRecordatorioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaRecordatorioService {

    private final CategoriaRecordatorioRepository repo;

    @Transactional(readOnly = true)
    public List<CategoriaRecordatorio> listarTodas() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public CategoriaRecordatorio obtenerPorId(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + id));
    }

    @Transactional
    public CategoriaRecordatorio crear(String nombre, String descripcion) {
        CategoriaRecordatorio c = new CategoriaRecordatorio();
        c.setNombre(nombre);
        c.setDescripcion(descripcion);
        c.setEstActivo(true);
        return repo.save(c);
    }

    @Transactional
    public CategoriaRecordatorio actualizar(Integer id, String nombre, String descripcion) {
        CategoriaRecordatorio c = obtenerPorId(id);
        if (nombre != null) c.setNombre(nombre);
        if (descripcion != null) c.setDescripcion(descripcion);
        return repo.save(c);
    }

    @Transactional
    public void desactivar(Integer id) {
        CategoriaRecordatorio c = obtenerPorId(id);
        c.setEstActivo(false);
        repo.save(c);
    }

    @Transactional(readOnly = true)
    public List<CategoriaRecordatorioResponseDto> listarTodasDto() {
        return listarTodas().stream().map(CategoriaRecordatorioResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaRecordatorioResponseDto obtenerDtoPorId(Integer id) {
        return CategoriaRecordatorioResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public CategoriaRecordatorioResponseDto crearDto(String nombre, String descripcion) {
        return CategoriaRecordatorioResponseDto.from(crear(nombre, descripcion));
    }

    @Transactional
    public CategoriaRecordatorioResponseDto actualizarDto(Integer id, String nombre, String descripcion) {
        return CategoriaRecordatorioResponseDto.from(actualizar(id, nombre, descripcion));
    }
}
