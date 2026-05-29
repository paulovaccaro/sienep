package edu.utec.sienep.service;

import edu.utec.sienep.dto.CarreraResponseDto;
import edu.utec.sienep.entity.Carrera;
import edu.utec.sienep.repository.CarreraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarreraService {

    private final CarreraRepository carreraRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<Carrera> listarActivas() {
        return carreraRepository.findByEstActivo(true);
    }

    @Transactional(readOnly = true)
    public List<Carrera> listarTodas() {
        return carreraRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Carrera obtenerPorId(Integer id) {
        return carreraRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Carrera no encontrada: " + id));
    }

    @Transactional
    public Carrera crear(String codigo, String nombre, String plan) {
        if (carreraRepository.existsByCodigo(codigo))
            throw new IllegalArgumentException("Ya existe una carrera con código: " + codigo);

        Carrera carrera = new Carrera();
        carrera.setCodigo(codigo);
        carrera.setNombre(nombre);
        carrera.setPlan(plan);
        carrera.setEstActivo(true);
        Carrera saved = carreraRepository.save(carrera);
        auditoriaService.registrar("CREAR", "carreras", String.valueOf(saved.getIdCarrera()), codigo);
        return saved;
    }

    @Transactional
    public Carrera actualizar(Integer id, String codigo, String nombre, String plan, Boolean estActivo) {
        Carrera carrera = obtenerPorId(id);
        if (codigo != null) carrera.setCodigo(codigo);
        if (nombre != null) carrera.setNombre(nombre);
        if (plan != null) carrera.setPlan(plan);
        if (estActivo != null) carrera.setEstActivo(estActivo);
        Carrera saved = carreraRepository.save(carrera);
        auditoriaService.registrar("MODIFICAR", "carreras", String.valueOf(id), null);
        return saved;
    }

    @Transactional
    public void desactivar(Integer id) {
        Carrera carrera = obtenerPorId(id);
        carrera.setEstActivo(false);
        carreraRepository.save(carrera);
        auditoriaService.registrar("BAJA", "carreras", String.valueOf(id), null);
    }

    @Transactional(readOnly = true)
    public List<CarreraResponseDto> listarActivasDto() {
        return listarActivas().stream().map(CarreraResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public CarreraResponseDto obtenerDtoPorId(Integer id) {
        return CarreraResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public CarreraResponseDto crearDto(String codigo, String nombre, String plan) {
        return CarreraResponseDto.from(crear(codigo, nombre, plan));
    }

    @Transactional
    public CarreraResponseDto actualizarDto(Integer id, String codigo, String nombre, String plan, Boolean estActivo) {
        return CarreraResponseDto.from(actualizar(id, codigo, nombre, plan, estActivo));
    }
}
