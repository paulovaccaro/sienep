package edu.utec.sienep.service;

import edu.utec.sienep.dto.FuncionarioResponseDto;
import edu.utec.sienep.entity.Funcionario;
import edu.utec.sienep.entity.Usuario;
import edu.utec.sienep.repository.FuncionarioRepository;
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
public class FuncionarioService {

    private final UsuarioRepository usuarioRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Transactional
    public Funcionario registrar(String cedula, String nombre, String apellido,
                                 String password, LocalDate fecNacimiento) {

        if (!ValidadorCI.validarCI(cedula))
            throw new IllegalArgumentException("CI inválida: " + cedula);
        cedula = ValidadorCI.normalizar(cedula);
        if (!ValidadorEdad.esMayorDe18(fecNacimiento))
            throw new IllegalArgumentException("El funcionario debe ser mayor de 18 años");
        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        if (usuarioRepository.existsByCedula(cedula))
            throw new IllegalArgumentException("Ya existe un usuario con esa CI");

        String username = nombre.toLowerCase() + "." + apellido.toLowerCase();
        String correo = username + "@utec.edu.uy";

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

        Funcionario funcionario = new Funcionario();
        funcionario.setUsuario(usuario);
        funcionario.setEstActivo(false);
        Funcionario saved = funcionarioRepository.save(funcionario);
        auditoriaService.registrar("CREAR", "funcionarios", String.valueOf(usuario.getIdUsuario()), cedula);
        return saved;
    }

    @Transactional(readOnly = true)
    public Funcionario obtenerPorId(Integer idUsuario) {
        return funcionarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Funcionario no encontrado: " + idUsuario));
    }

    @Transactional(readOnly = true)
    public List<Funcionario> listarTodos() {
        return funcionarioRepository.findAll();
    }

    @Transactional
    public void desactivar(Integer idUsuario) {
        Funcionario funcionario = obtenerPorId(idUsuario);
        funcionario.setEstActivo(false);
        funcionarioRepository.save(funcionario);
        auditoriaService.registrar("BAJA", "funcionarios", String.valueOf(idUsuario), null);
    }

    @Transactional
    public Funcionario activar(Integer idUsuario) {
        Funcionario funcionario = obtenerPorId(idUsuario);
        funcionario.setEstActivo(true);
        funcionario.getUsuario().setEstActivo(true);
        usuarioRepository.save(funcionario.getUsuario());
        Funcionario saved = funcionarioRepository.save(funcionario);
        auditoriaService.registrar("MODIFICAR", "funcionarios", String.valueOf(idUsuario), null);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<FuncionarioResponseDto> listarTodosDto() {
        return listarTodos().stream().map(FuncionarioResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public FuncionarioResponseDto obtenerDtoPorId(Integer idUsuario) {
        return FuncionarioResponseDto.from(obtenerPorId(idUsuario));
    }

    @Transactional
    public FuncionarioResponseDto registrarDto(String cedula, String nombre, String apellido,
                                               String password, LocalDate fecNacimiento) {
        return FuncionarioResponseDto.from(registrar(cedula, nombre, apellido, password, fecNacimiento));
    }
}
