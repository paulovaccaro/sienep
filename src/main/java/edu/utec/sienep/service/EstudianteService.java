package edu.utec.sienep.service;

import edu.utec.sienep.dto.EstudianteResponseDto;
import edu.utec.sienep.entity.Estudiante;
import edu.utec.sienep.entity.Grupo;
import edu.utec.sienep.entity.Usuario;
import edu.utec.sienep.repository.EstudianteRepository;
import edu.utec.sienep.repository.GrupoRepository;
import edu.utec.sienep.repository.UsuarioRepository;
import edu.utec.sienep.util.ValidadorCI;
import edu.utec.sienep.util.ValidadorEdad;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstudianteService {

    private final UsuarioRepository usuarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final GrupoRepository grupoRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Transactional
    public Estudiante registrar(String cedula, String nombre, String apellido,
                                String password, LocalDate fecNacimiento, Integer idGrupo) {

        if (!ValidadorCI.validarCI(cedula))
            throw new IllegalArgumentException("CI inválida: " + cedula);
        cedula = ValidadorCI.normalizar(cedula);
        if (!ValidadorEdad.esMayorDe18(fecNacimiento))
            throw new IllegalArgumentException("El estudiante debe ser mayor de 18 años");
        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        if (usuarioRepository.existsByCedula(cedula))
            throw new IllegalArgumentException("Ya existe un usuario con esa CI");

        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));

        String baseUsername = nombre.toLowerCase() + "." + apellido.toLowerCase();
        String username = baseUsername;
        if (usuarioRepository.existsByUsername(username)) {
            username = baseUsername + "." + cedula.substring(cedula.length() - 3);
        }

        // correo_uy requires ^[a-z]+\.[a-z]+@estudiantes.utec.edu.uy$ — no digits or extra dots
        String correoBase = baseUsername;
        String correo = correoBase + "@estudiantes.utec.edu.uy";
        if (usuarioRepository.existsByCorreo(correo)) {
            for (char c = 'b'; c <= 'z'; c++) {
                String candidate = correoBase + c + "@estudiantes.utec.edu.uy";
                if (!usuarioRepository.existsByCorreo(candidate)) {
                    correo = candidate;
                    break;
                }
            }
        }

        Usuario usuario = new Usuario();
        usuario.setCedula(cedula);
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setCorreo(correo);
        usuario.setFecNacimiento(fecNacimiento);
        usuario.setEstActivo(false);
        usuario = usuarioRepository.save(usuario);

        Estudiante estudiante = new Estudiante();
        estudiante.setUsuario(usuario);
        estudiante.setGrupo(grupo);
        estudiante.setEstActivo(false);
        Estudiante saved = estudianteRepository.save(estudiante);
        auditoriaService.registrar("CREAR", "estudiantes", String.valueOf(saved.getUsuario().getIdUsuario()), cedula);
        return saved;
    }

    @Transactional(readOnly = true)
    public Estudiante obtenerPorId(Integer idUsuario) {
        return estudianteRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + idUsuario));
    }

    @Transactional(readOnly = true)
    public List<Estudiante> listarTodos() {
        return estudianteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Estudiante> listarPorGrupos(List<Integer> idGrupos) {
        if (idGrupos.isEmpty()) return List.of();
        return estudianteRepository.findByGruposIn(idGrupos);
    }

    @Transactional
    public Estudiante actualizar(Integer idUsuario, String nombre, String apellido,
                                 Integer idGrupo, Boolean estActivo) {

        Estudiante estudiante = obtenerPorId(idUsuario);
        Usuario usuario = estudiante.getUsuario();

        if (nombre != null) usuario.setNombre(nombre);
        if (apellido != null) usuario.setApellido(apellido);
        if (nombre != null || apellido != null) {
            String base = usuario.getNombre().toLowerCase() + "." + usuario.getApellido().toLowerCase();
            String nuevoUsername = base;
            if (!nuevoUsername.equals(usuario.getUsername()) && usuarioRepository.existsByUsername(nuevoUsername)) {
                nuevoUsername = base + "." + usuario.getCedula().substring(usuario.getCedula().length() - 3);
            }
            usuario.setUsername(nuevoUsername);
            String nuevoCorreo = base + "@estudiantes.utec.edu.uy";
            if (!nuevoCorreo.equals(usuario.getCorreo()) && usuarioRepository.existsByCorreo(nuevoCorreo)) {
                for (char c = 'b'; c <= 'z'; c++) {
                    String candidate = base + c + "@estudiantes.utec.edu.uy";
                    if (!usuarioRepository.existsByCorreo(candidate)) {
                        nuevoCorreo = candidate;
                        break;
                    }
                }
            }
            usuario.setCorreo(nuevoCorreo);
        }

        if (idGrupo != null) {
            Grupo grupo = grupoRepository.findById(idGrupo)
                    .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));
            estudiante.setGrupo(grupo);
        }

        if (estActivo != null) {
            estudiante.setEstActivo(estActivo);
            usuario.setEstActivo(estActivo);
        }

        usuarioRepository.save(usuario);
        Estudiante saved = estudianteRepository.save(estudiante);
        auditoriaService.registrar("MODIFICAR", "estudiantes", String.valueOf(idUsuario), nombre);
        return saved;
    }

    @Transactional
    public void desactivar(Integer idUsuario) {
        Estudiante estudiante = obtenerPorId(idUsuario);
        estudiante.setEstActivo(false);
        estudianteRepository.save(estudiante);
        auditoriaService.registrar("BAJA", "estudiantes", String.valueOf(idUsuario), null);
    }

    @Transactional(readOnly = true)
    public List<EstudianteResponseDto> listarPorGruposDto(List<Integer> idGrupos) {
        return listarPorGrupos(idGrupos).stream().map(EstudianteResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public EstudianteResponseDto obtenerDtoPorId(Integer idUsuario) {
        return EstudianteResponseDto.from(obtenerPorId(idUsuario));
    }

    @Transactional
    public EstudianteResponseDto registrarDto(String cedula, String nombre, String apellido,
                                              String password, LocalDate fecNacimiento, Integer idGrupo) {
        return EstudianteResponseDto.from(registrar(cedula, nombre, apellido, password, fecNacimiento, idGrupo));
    }

    @Transactional
    public EstudianteResponseDto actualizarDto(Integer idUsuario, String nombre, String apellido,
                                               Integer idGrupo, Boolean estActivo) {
        return EstudianteResponseDto.from(actualizar(idUsuario, nombre, apellido, idGrupo, estActivo));
    }
}
