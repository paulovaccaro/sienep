package edu.utec.sienep.service;

import edu.utec.sienep.entity.Usuario;
import edu.utec.sienep.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public Usuario autenticar(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!usuario.getEstActivo()) {
            throw new IllegalStateException("El usuario no está activo. Debe aceptar las políticas de uso.");
        }

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        auditoriaService.registrar(usuario.getIdUsuario(), "LOGIN", "usuarios",
                String.valueOf(usuario.getIdUsuario()), null);
        return usuario;
    }

    @Transactional
    public Usuario aceptarPoliticas(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        if (usuario.getEstActivo()) {
            throw new IllegalStateException("El usuario ya está activo.");
        }

        usuario.setEstActivo(true);
        usuario = usuarioRepository.save(usuario);
        auditoriaService.registrar(usuario.getIdUsuario(), "ACEPTAR_POLITICAS", "usuarios",
                String.valueOf(usuario.getIdUsuario()), null);
        return usuario;
    }

    /** RF03 – Cambio de contraseña del usuario autenticado. */
    @Transactional
    public void cambiarPassword(Integer idUsuario, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (!passwordEncoder.matches(passwordActual, usuario.getPassword()))
            throw new IllegalArgumentException("La contraseña actual es incorrecta");

        if (passwordNueva == null || passwordNueva.length() < 8)
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 8 caracteres");

        usuario.setPassword(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
        auditoriaService.registrar(idUsuario, "CAMBIAR_PASSWORD", "usuarios",
                String.valueOf(idUsuario), null);
    }
}
