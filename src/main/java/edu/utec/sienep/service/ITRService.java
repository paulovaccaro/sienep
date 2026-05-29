package edu.utec.sienep.service;

import edu.utec.sienep.dto.ITRResponseDto;
import edu.utec.sienep.entity.Direccion;
import edu.utec.sienep.entity.ITR;
import edu.utec.sienep.repository.DireccionRepository;
import edu.utec.sienep.repository.ITRRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ITRService {

    private final ITRRepository itrRepository;
    private final DireccionRepository direccionRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<ITR> listarActivos() {
        return itrRepository.findByEstActivo(true);
    }

    @Transactional(readOnly = true)
    public List<ITR> listarTodos() {
        return itrRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ITR obtenerPorId(Integer id) {
        return itrRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ITR no encontrado: " + id));
    }

    @Transactional
    public ITR crear(String codigo, String nombre, Integer idDireccion) {
        if (itrRepository.existsByCodigo(codigo))
            throw new IllegalArgumentException("Ya existe un ITR con código: " + codigo);

        Direccion direccion = direccionRepository.findById(idDireccion)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada: " + idDireccion));

        ITR itr = new ITR();
        itr.setCodigo(codigo);
        itr.setNombre(nombre);
        itr.setDireccion(direccion);
        itr.setEstActivo(true);
        ITR saved = itrRepository.save(itr);
        auditoriaService.registrar("CREAR", "itr", String.valueOf(saved.getIdItr()), codigo);
        return saved;
    }

    @Transactional
    public ITR actualizar(Integer id, String codigo, String nombre, Boolean estActivo) {
        ITR itr = obtenerPorId(id);
        if (codigo != null) itr.setCodigo(codigo);
        if (nombre != null) itr.setNombre(nombre);
        if (estActivo != null) itr.setEstActivo(estActivo);
        ITR saved = itrRepository.save(itr);
        auditoriaService.registrar("MODIFICAR", "itr", String.valueOf(id), null);
        return saved;
    }

    @Transactional
    public void desactivar(Integer id) {
        ITR itr = obtenerPorId(id);
        itr.setEstActivo(false);
        itrRepository.save(itr);
        auditoriaService.registrar("BAJA", "itr", String.valueOf(id), null);
    }

    @Transactional(readOnly = true)
    public List<ITRResponseDto> listarActivosDto() {
        return listarActivos().stream().map(ITRResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ITRResponseDto obtenerDtoPorId(Integer id) {
        return ITRResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public ITRResponseDto crearDto(String codigo, String nombre, Integer idDireccion) {
        return ITRResponseDto.from(crear(codigo, nombre, idDireccion));
    }

    @Transactional
    public ITRResponseDto actualizarDto(Integer id, String codigo, String nombre, Boolean estActivo) {
        return ITRResponseDto.from(actualizar(id, codigo, nombre, estActivo));
    }
}
