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
        return usuarioRepository.save(usuario);
    }
}
