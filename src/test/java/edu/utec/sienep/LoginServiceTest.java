package edu.utec.sienep;

import edu.utec.sienep.entity.Usuario;
import edu.utec.sienep.repository.UsuarioRepository;
import edu.utec.sienep.service.AuditoriaService;
import edu.utec.sienep.service.LoginService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para LoginService (RF01, RF02, RF03, RF04).
 */
@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuditoriaService auditoriaService;

    @InjectMocks LoginService loginService;

    private Usuario usuarioActivo(String username, String encodedPass) {
        Usuario u = new Usuario();
        u.setIdUsuario(1);
        u.setUsername(username);
        u.setPassword(encodedPass);
        u.setEstActivo(true);
        return u;
    }

    // ── autenticar ────────────────────────────────────────────────────────────

    @Test
    void autenticar_credencialesValidas_retornaUsuario() {
        Usuario u = usuarioActivo("juan.perez", "hashed");
        when(usuarioRepository.findByUsername("juan.perez")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("pass1234", "hashed")).thenReturn(true);

        Usuario result = loginService.autenticar("juan.perez", "pass1234");

        assertThat(result).isEqualTo(u);
        verify(auditoriaService).registrar(eq(1), eq("LOGIN"), eq("usuarios"), any(), isNull());
    }

    @Test
    void autenticar_usuarioInexistente_lanzaIllegalArgument() {
        when(usuarioRepository.findByUsername("no.existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.autenticar("no.existe", "pass1234"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void autenticar_passwordIncorrecta_lanzaIllegalArgument() {
        Usuario u = usuarioActivo("juan.perez", "hashed");
        when(usuarioRepository.findByUsername("juan.perez")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("wrongPass", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> loginService.autenticar("juan.perez", "wrongPass"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void autenticar_usuarioInactivo_lanzaIllegalState() {
        Usuario u = new Usuario();
        u.setIdUsuario(2);
        u.setUsername("inactivo.user");
        u.setPassword("hashed");
        u.setEstActivo(false);
        when(usuarioRepository.findByUsername("inactivo.user")).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> loginService.autenticar("inactivo.user", "pass1234"))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── cambiarPassword ───────────────────────────────────────────────────────

    @Test
    void cambiarPassword_correcta_actualizaPasswordYAudita() {
        Usuario u = new Usuario();
        u.setIdUsuario(1);
        u.setPassword("hashed_old");
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("oldPass1", "hashed_old")).thenReturn(true);
        when(passwordEncoder.encode("newPass12")).thenReturn("hashed_new");

        loginService.cambiarPassword(1, "oldPass1", "newPass12");

        assertThat(u.getPassword()).isEqualTo("hashed_new");
        verify(usuarioRepository).save(u);
        verify(auditoriaService).registrar(eq(1), eq("CAMBIAR_PASSWORD"), eq("usuarios"), any(), isNull());
    }

    @Test
    void cambiarPassword_passwordActualIncorrecta_lanzaIllegalArgument() {
        Usuario u = new Usuario();
        u.setPassword("hashed");
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("wrongOld", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> loginService.cambiarPassword(1, "wrongOld", "newPass12"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarPassword_nuevaPasswordCorta_lanzaIllegalArgument() {
        Usuario u = new Usuario();
        u.setPassword("hashed");
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("oldPass1", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> loginService.cambiarPassword(1, "oldPass1", "abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("8");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarPassword_usuarioInexistente_lanzaIllegalArgument() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.cambiarPassword(99, "old", "newPass12"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
