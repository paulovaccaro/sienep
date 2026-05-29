package edu.utec.sienep.service;

import edu.utec.sienep.dto.IncidenciaResponseDto;
import edu.utec.sienep.entity.Incidencia;
import edu.utec.sienep.entity.Instancia;
import edu.utec.sienep.repository.IncidenciaRepository;
import edu.utec.sienep.repository.InstanciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final InstanciaRepository instanciaRepository;
    private final AuditoriaService auditoriaService;

    @Transactional
    public Incidencia crear(Integer idInstancia, String lugar) {
        if (incidenciaRepository.existsById(idInstancia))
            throw new IllegalArgumentException("Ya existe una incidencia para la instancia: " + idInstancia);

        Instancia instancia = instanciaRepository.findById(idInstancia)
                .orElseThrow(() -> new IllegalArgumentException("Instancia no encontrada: " + idInstancia));

        Incidencia inc = new Incidencia();
        inc.setInstancia(instancia);
        inc.setLugar(lugar);
        Incidencia saved = incidenciaRepository.save(inc);
        auditoriaService.registrar("CREAR", "incidencias", String.valueOf(saved.getInstancia().getIdInstancia()), lugar);
        return saved;
    }

    @Transactional(readOnly = true)
    public Incidencia obtenerPorId(Integer id) {
        return incidenciaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Incidencia no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<Incidencia> listarTodos() {
        return incidenciaRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public List<Incidencia> listarPorFuncionario(Integer idFuncionario) {
        return incidenciaRepository.findByFuncionarioWithDetails(idFuncionario);
    }

    @Transactional
    public Incidencia actualizar(Integer id, String lugar) {
        Incidencia inc = obtenerPorId(id);
        if (lugar != null) inc.setLugar(lugar);
        return incidenciaRepository.save(inc);
    }

    @Transactional
    public void desactivar(Integer id) {
        Incidencia inc = obtenerPorId(id);
        inc.getInstancia().setEstActivo(false);
        instanciaRepository.save(inc.getInstancia());
        auditoriaService.registrar("BAJA", "incidencias", String.valueOf(id), null);
    }

    @Transactional(readOnly = true)
    public List<IncidenciaResponseDto> listarTodosDto() {
        return listarTodos().stream().map(IncidenciaResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public IncidenciaResponseDto obtenerDtoPorId(Integer id) {
        return IncidenciaResponseDto.from(obtenerPorId(id));
    }

    @Transactional(readOnly = true)
    public List<IncidenciaResponseDto> listarPorFuncionarioDto(Integer idFuncionario) {
        return listarPorFuncionario(idFuncionario).stream().map(IncidenciaResponseDto::from).toList();
    }

    @Transactional
    public IncidenciaResponseDto crearDto(Integer idInstancia, String lugar) {
        return IncidenciaResponseDto.from(crear(idInstancia, lugar));
    }

    @Transactional
    public IncidenciaResponseDto actualizarDto(Integer id, String lugar) {
        return IncidenciaResponseDto.from(actualizar(id, lugar));
    }
}
