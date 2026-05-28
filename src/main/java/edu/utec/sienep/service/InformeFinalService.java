package edu.utec.sienep.service;

import edu.utec.sienep.dto.InformeFinalResponseDto;
import edu.utec.sienep.entity.InformeFinal;
import edu.utec.sienep.repository.InformeFinalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InformeFinalService {

    private final InformeFinalRepository informeFinalRepository;

    @Transactional
    public InformeFinal crear(String contenido, Integer valoracion, LocalDate fecCreacion) {
        InformeFinal inf = new InformeFinal();
        inf.setContenido(contenido);
        inf.setValoracion(valoracion);
        inf.setFecCreacion(fecCreacion != null ? fecCreacion : LocalDate.now());
        inf.setEstActivo(true);
        return informeFinalRepository.save(inf);
    }

    @Transactional(readOnly = true)
    public InformeFinal obtenerPorId(Integer id) {
        return informeFinalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Informe final no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<InformeFinal> listarTodos() {
        return informeFinalRepository.findAll();
    }

    @Transactional
    public InformeFinal actualizar(Integer id, String contenido, Integer valoracion, LocalDate fecCreacion) {
        InformeFinal inf = obtenerPorId(id);
        if (contenido != null) inf.setContenido(contenido);
        if (valoracion != null) inf.setValoracion(valoracion);
        if (fecCreacion != null) inf.setFecCreacion(fecCreacion);
        return informeFinalRepository.save(inf);
    }

    @Transactional
    public void desactivar(Integer id) {
        InformeFinal inf = obtenerPorId(id);
        inf.setEstActivo(false);
        informeFinalRepository.save(inf);
    }

    @Transactional(readOnly = true)
    public List<InformeFinalResponseDto> listarTodosDto() {
        return listarTodos().stream().map(InformeFinalResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public InformeFinalResponseDto obtenerDtoPorId(Integer id) {
        return InformeFinalResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public InformeFinalResponseDto crearDto(String contenido, Integer valoracion, java.time.LocalDate fecCreacion) {
        return InformeFinalResponseDto.from(crear(contenido, valoracion, fecCreacion));
    }

    @Transactional
    public InformeFinalResponseDto actualizarDto(Integer id, String contenido, Integer valoracion,
                                                  java.time.LocalDate fecCreacion) {
        return InformeFinalResponseDto.from(actualizar(id, contenido, valoracion, fecCreacion));
    }
}
