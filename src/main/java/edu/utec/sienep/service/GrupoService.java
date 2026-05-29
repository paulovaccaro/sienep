package edu.utec.sienep.service;

import edu.utec.sienep.dto.GrupoResponseDto;
import edu.utec.sienep.entity.Carrera;
import edu.utec.sienep.entity.Grupo;
import edu.utec.sienep.entity.ITR;
import edu.utec.sienep.repository.CarreraRepository;
import edu.utec.sienep.repository.GrupoRepository;
import edu.utec.sienep.repository.ITRRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrupoService {

    private final GrupoRepository grupoRepository;
    private final CarreraRepository carreraRepository;
    private final ITRRepository itrRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<Grupo> listarTodos() {
        return grupoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Grupo> listarPorItr(Integer idItr) {
        return grupoRepository.findByItr_IdItr(idItr);
    }

    @Transactional(readOnly = true)
    public List<Grupo> listarActivos() {
        return grupoRepository.findByEstActivo(true);
    }

    @Transactional(readOnly = true)
    public Grupo obtenerPorId(Integer id) {
        return grupoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + id));
    }

    @Transactional
    public Grupo crear(String nomGrupo, Integer idCarrera, Integer idItr, Integer anio, Integer semestre) {
        Carrera carrera = carreraRepository.findById(idCarrera)
                .orElseThrow(() -> new IllegalArgumentException("Carrera no encontrada: " + idCarrera));
        ITR itr = itrRepository.findById(idItr)
                .orElseThrow(() -> new IllegalArgumentException("ITR no encontrado: " + idItr));

        Grupo grupo = new Grupo();
        grupo.setNomGrupo(nomGrupo);
        grupo.setCarrera(carrera);
        grupo.setItr(itr);
        grupo.setAnio(anio);
        grupo.setSemestre(semestre);
        grupo.setEstActivo(true);
        Grupo saved = grupoRepository.save(grupo);
        auditoriaService.registrar("CREAR", "grupos", String.valueOf(saved.getIdGrupo()), nomGrupo);
        return saved;
    }

    @Transactional
    public Grupo actualizar(Integer id, String nomGrupo, Boolean estActivo) {
        Grupo grupo = obtenerPorId(id);
        if (nomGrupo != null) grupo.setNomGrupo(nomGrupo);
        if (estActivo != null) grupo.setEstActivo(estActivo);
        Grupo saved = grupoRepository.save(grupo);
        auditoriaService.registrar("MODIFICAR", "grupos", String.valueOf(id), null);
        return saved;
    }

    @Transactional
    public void desactivar(Integer id) {
        Grupo grupo = obtenerPorId(id);
        grupo.setEstActivo(false);
        grupoRepository.save(grupo);
        auditoriaService.registrar("BAJA", "grupos", String.valueOf(id), null);
    }

    @Transactional(readOnly = true)
    public List<GrupoResponseDto> listarActivosDto() {
        return listarActivos().stream().map(GrupoResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<GrupoResponseDto> listarPorItrDto(Integer idItr) {
        return listarPorItr(idItr).stream().map(GrupoResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public GrupoResponseDto obtenerDtoPorId(Integer id) {
        return GrupoResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public GrupoResponseDto crearDto(String nomGrupo, Integer idCarrera, Integer idItr,
                                     Integer anio, Integer semestre) {
        return GrupoResponseDto.from(crear(nomGrupo, idCarrera, idItr, anio, semestre));
    }

    @Transactional
    public GrupoResponseDto actualizarDto(Integer id, String nomGrupo, Boolean estActivo) {
        return GrupoResponseDto.from(actualizar(id, nomGrupo, estActivo));
    }
}
