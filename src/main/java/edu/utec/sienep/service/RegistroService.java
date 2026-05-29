package edu.utec.sienep.service;

import edu.utec.sienep.entity.*;
import edu.utec.sienep.repository.*;

import edu.utec.sienep.util.ValidadorCI;
import edu.utec.sienep.util.ValidadorEdad;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final RolRepository rolRepository;
    private final ITRRepository itrRepository;
    private final CarreraRepository carreraRepository;
    private final GrupoRepository grupoRepository;
    private final AsignacionRepository asignacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Transactional
    public Usuario registrarFuncionario(String cedula, String nombre, String apellido,
                                        String password, LocalDate fecNacimiento, Integer idRol,
                                        Integer idItr, Integer idCarrera, Integer idGrupo) {

        if (!ValidadorCI.validarCI(cedula))
            throw new IllegalArgumentException("CI inválida: " + cedula);
        cedula = ValidadorCI.normalizar(cedula);
        if (!ValidadorEdad.esMayorDe18(fecNacimiento))
            throw new IllegalArgumentException("Debe ser mayor de 18 años");
        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        if (usuarioRepository.existsByCedula(cedula))
            throw new IllegalArgumentException("Ya existe un usuario con esa CI");

        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + idRol));

        String baseUsername = nombre.toLowerCase() + "." + apellido.toLowerCase();
        String username = baseUsername;
        if (usuarioRepository.existsByUsername(username)) {
            username = baseUsername + "." + cedula.substring(cedula.length() - 3);
        }

        // correo_uy requires ^[a-z]+\.[a-z]+@utec.edu.uy$ — no digits or extra dots allowed
        String correoBase = nombre.toLowerCase() + "." + apellido.toLowerCase();
        String correo = correoBase + "@utec.edu.uy";
        if (usuarioRepository.existsByCorreo(correo)) {
            for (char c = 'b'; c <= 'z'; c++) {
                String candidate = correoBase + c + "@utec.edu.uy";
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
        usuario.setEstActivo(true);
        usuario = usuarioRepository.save(usuario);

        Funcionario funcionario = new Funcionario();
        funcionario.setUsuario(usuario);
        funcionario.setEstActivo(true);
        funcionarioRepository.save(funcionario);

        ITR itr = idItr != null
                ? itrRepository.findById(idItr).orElseThrow(() -> new IllegalArgumentException("ITR no encontrado: " + idItr))
                : null;
        Carrera carrera = idCarrera != null
                ? carreraRepository.findById(idCarrera).orElseThrow(() -> new IllegalArgumentException("Carrera no encontrada: " + idCarrera))
                : null;
        Grupo grupo = idGrupo != null
                ? grupoRepository.findById(idGrupo).orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo))
                : null;

        Asignacion asignacion = new Asignacion();
        asignacion.setUsuario(usuario);
        asignacion.setRol(rol);
        asignacion.setItr(itr);
        asignacion.setCarrera(carrera);
        asignacion.setGrupo(grupo);
        asignacion.setFecCreacion(OffsetDateTime.now());
        asignacion.setEstActivo(true);
        asignacionRepository.save(asignacion);

        auditoriaService.registrar(usuario.getIdUsuario(), "CREAR", "funcionarios",
                String.valueOf(usuario.getIdUsuario()), cedula);
        return usuario;
    }
}
