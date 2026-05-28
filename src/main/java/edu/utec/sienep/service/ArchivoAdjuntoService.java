package edu.utec.sienep.service;

import edu.utec.sienep.dto.ArchivoAdjuntoResponseDto;
import edu.utec.sienep.entity.ArchivoAdjunto;
import edu.utec.sienep.entity.Estudiante;
import edu.utec.sienep.entity.Usuario;
import edu.utec.sienep.repository.ArchivoAdjuntoRepository;
import edu.utec.sienep.repository.EstudianteRepository;
import edu.utec.sienep.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArchivoAdjuntoService {

    private final ArchivoAdjuntoRepository archivoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;

    @Transactional(readOnly = true)
    public List<ArchivoAdjunto> listarPorEstudiante(Integer idEstudiante) {
        return archivoRepository.findByEstudianteIdWithDetails(idEstudiante);
    }

    @Transactional(readOnly = true)
    public ArchivoAdjunto obtenerPorId(Integer id) {
        return archivoRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Archivo adjunto no encontrado: " + id));
    }

    @Transactional
    public ArchivoAdjunto crear(Integer idUsuario, Integer idEstudiante, String ruta, String categoria) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));
        Estudiante estudiante = estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + idEstudiante));

        ArchivoAdjunto archivo = new ArchivoAdjunto();
        archivo.setUsuario(usuario);
        archivo.setEstudiante(estudiante);
        archivo.setRuta(ruta);
        archivo.setCategoria(categoria);
        archivo.setEstActivo(true);
        return archivoRepository.save(archivo);
    }

    @Transactional
    public void desactivar(Integer id) {
        ArchivoAdjunto archivo = obtenerPorId(id);
        archivo.setEstActivo(false);
        archivoRepository.save(archivo);
    }

    @Transactional(readOnly = true)
    public List<ArchivoAdjuntoResponseDto> listarPorEstudianteDto(Integer idEstudiante) {
        return listarPorEstudiante(idEstudiante).stream().map(ArchivoAdjuntoResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public ArchivoAdjuntoResponseDto obtenerDtoPorId(Integer id) {
        return ArchivoAdjuntoResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public ArchivoAdjuntoResponseDto crearDto(Integer idUsuario, Integer idEstudiante,
                                               String ruta, String categoria) {
        return ArchivoAdjuntoResponseDto.from(crear(idUsuario, idEstudiante, ruta, categoria));
    }
}
