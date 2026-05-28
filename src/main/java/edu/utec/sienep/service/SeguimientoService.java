package edu.utec.sienep.service;

import edu.utec.sienep.dto.SeguimientoResponseDto;
import edu.utec.sienep.entity.Estudiante;
import edu.utec.sienep.entity.InformeFinal;
import edu.utec.sienep.entity.Seguimiento;
import edu.utec.sienep.repository.EstudianteRepository;
import edu.utec.sienep.repository.InformeFinalRepository;
import edu.utec.sienep.repository.SeguimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeguimientoService {

    private final SeguimientoRepository seguimientoRepository;
    private final EstudianteRepository estudianteRepository;
    private final InformeFinalRepository informeFinalRepository;

    @Transactional
    public Seguimiento agregar(Integer idInforme, Integer idEstudiante,
                               LocalDate fecInicio, LocalDate fecCierre, boolean estActivo) {

        Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + idEstudiante));

        InformeFinal informe = null;
        if (idInforme != null) {
            informe = informeFinalRepository.findById(idInforme)
                    .orElseThrow(() -> new IllegalArgumentException("Informe no encontrado: " + idInforme));
        }

        Seguimiento s = new Seguimiento();
        s.setEstudiante(estudiante);
        s.setInforme(informe);
        s.setFecInicio(fecInicio);
        s.setFecCierre(fecCierre);
        s.setEstActivo(estActivo);
        return seguimientoRepository.save(s);
    }

    @Transactional(readOnly = true)
    public Seguimiento buscarPorId(Integer idSeguimiento) {
        return seguimientoRepository.findByIdWithDetails(idSeguimiento)
                .orElseThrow(() -> new IllegalArgumentException("Seguimiento no encontrado: " + idSeguimiento));
    }

    @Transactional(readOnly = true)
    public List<Seguimiento> listarTodos() {
        return seguimientoRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public List<Seguimiento> listarPorEstudiante(Integer idEstudiante) {
        return seguimientoRepository.findByEstudianteIdWithDetails(idEstudiante);
    }

    @Transactional(readOnly = true)
    public List<Seguimiento> listarPorGrupos(List<Integer> idGrupos) {
        if (idGrupos.isEmpty()) return List.of();
        return seguimientoRepository.findByGruposIn(idGrupos);
    }

    @Transactional
    public Seguimiento actualizar(Integer idSeguimiento, Integer idInforme, Integer idEstudiante,
                                  LocalDate fecInicio, LocalDate fecCierre, boolean estActivo) {

        Seguimiento s = buscarPorId(idSeguimiento);

        if (idEstudiante != null) {
            Estudiante est = estudianteRepository.findById(idEstudiante)
                    .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + idEstudiante));
            s.setEstudiante(est);
        }

        if (idInforme != null) {
            InformeFinal inf = informeFinalRepository.findById(idInforme)
                    .orElseThrow(() -> new IllegalArgumentException("Informe no encontrado: " + idInforme));
            s.setInforme(inf);
        } else {
            s.setInforme(null);
        }

        s.setFecInicio(fecInicio);
        s.setFecCierre(fecCierre);
        s.setEstActivo(estActivo);
        return seguimientoRepository.save(s);
    }

    @Transactional
    public void eliminar(Integer idSeguimiento) {
        Seguimiento s = buscarPorId(idSeguimiento);
        seguimientoRepository.delete(s);
    }

    @Transactional(readOnly = true)
    public boolean tieneSeguimientoActivo(Integer idEstudiante) {
        return seguimientoRepository.tieneSeguimientoActivo(idEstudiante);
    }

    @Transactional(readOnly = true)
    public List<SeguimientoResponseDto> listarPorGruposDto(List<Integer> idGrupos) {
        return listarPorGrupos(idGrupos).stream().map(SeguimientoResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public SeguimientoResponseDto buscarDtoPorId(Integer idSeguimiento) {
        return SeguimientoResponseDto.from(buscarPorId(idSeguimiento));
    }

    @Transactional(readOnly = true)
    public List<SeguimientoResponseDto> listarPorEstudianteDto(Integer idEstudiante) {
        return listarPorEstudiante(idEstudiante).stream().map(SeguimientoResponseDto::from).toList();
    }

    @Transactional
    public SeguimientoResponseDto agregarDto(Integer idInforme, Integer idEstudiante,
                                             java.time.LocalDate fecInicio, java.time.LocalDate fecCierre,
                                             boolean estActivo) {
        return SeguimientoResponseDto.from(agregar(idInforme, idEstudiante, fecInicio, fecCierre, estActivo));
    }

    @Transactional
    public SeguimientoResponseDto actualizarDto(Integer idSeguimiento, Integer idInforme, Integer idEstudiante,
                                                java.time.LocalDate fecInicio, java.time.LocalDate fecCierre,
                                                boolean estActivo) {
        return SeguimientoResponseDto.from(actualizar(idSeguimiento, idInforme, idEstudiante,
                fecInicio, fecCierre, estActivo));
    }
}
