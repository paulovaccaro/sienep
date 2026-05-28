package edu.utec.sienep.service;

import edu.utec.sienep.dto.ObservacionResponseDto;
import edu.utec.sienep.entity.Estudiante;
import edu.utec.sienep.entity.Funcionario;
import edu.utec.sienep.entity.Observacion;
import edu.utec.sienep.repository.EstudianteRepository;
import edu.utec.sienep.repository.FuncionarioRepository;
import edu.utec.sienep.repository.ObservacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ObservacionService {

    private final ObservacionRepository observacionRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final EstudianteRepository estudianteRepository;

    @Transactional(readOnly = true)
    public List<Observacion> listarPorEstudiante(Integer idEstudiante) {
        return observacionRepository.findByEstudianteIdWithDetails(idEstudiante);
    }

    @Transactional(readOnly = true)
    public Observacion obtenerPorId(Integer id) {
        return observacionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Observación no encontrada: " + id));
    }

    @Transactional
    public Observacion crear(Integer idFuncionario, Integer idEstudiante, String titulo, String contenido) {
        Funcionario funcionario = funcionarioRepository.findById(idFuncionario)
                .orElseThrow(() -> new IllegalArgumentException("Funcionario no encontrado: " + idFuncionario));
        Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + idEstudiante));

        Observacion obs = new Observacion();
        obs.setFuncionario(funcionario);
        obs.setEstudiante(estudiante);
        obs.setTitulo(titulo);
        obs.setContenido(contenido);
        obs.setFecHora(OffsetDateTime.now());
        obs.setEstActivo(true);
        return observacionRepository.save(obs);
    }

    @Transactional
    public void desactivar(Integer id) {
        Observacion obs = obtenerPorId(id);
        obs.setEstActivo(false);
        observacionRepository.save(obs);
    }

    @Transactional(readOnly = true)
    public List<ObservacionResponseDto> listarPorEstudianteDto(Integer idEstudiante) {
        return listarPorEstudiante(idEstudiante).stream().map(ObservacionResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ObservacionResponseDto obtenerDtoPorId(Integer id) {
        return ObservacionResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public ObservacionResponseDto crearDto(Integer idFuncionario, Integer idEstudiante,
                                           String titulo, String contenido) {
        return ObservacionResponseDto.from(crear(idFuncionario, idEstudiante, titulo, contenido));
    }
}
