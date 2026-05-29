package edu.utec.sienep;

import edu.utec.sienep.entity.Estudiante;
import edu.utec.sienep.entity.Grupo;
import edu.utec.sienep.entity.Usuario;
import edu.utec.sienep.repository.EstudianteRepository;
import edu.utec.sienep.repository.GrupoRepository;
import edu.utec.sienep.repository.UsuarioRepository;
import edu.utec.sienep.service.AuditoriaService;
import edu.utec.sienep.service.EstudianteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para EstudianteService (RF05, RF06, RF07, RF08).
 * CI válida usada: 12345672 (dígito verificador = 2).
 * CI inválida usada: 12345678 (dígito verificador correcto es 2, no 8).
 */
@ExtendWith(MockitoExtension.class)
class EstudianteServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock EstudianteRepository estudianteRepository;
    @Mock GrupoRepository grupoRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuditoriaService auditoriaService;

    @InjectMocks EstudianteService estudianteService;

    private static final String CI_VALIDA   = "12345672";
    private static final String CI_INVALIDA = "12345678";
    private static final LocalDate MAYOR_EDAD = LocalDate.of(2000, 1, 1);
    private static final LocalDate MENOR_EDAD = LocalDate.now().minusYears(16);

    private Grupo grupoMock() {
        Grupo g = new Grupo();
        g.setIdGrupo(1);
        g.setNomGrupo("TIC-SUR-2025-1");
        return g;
    }

    private Usuario usuarioSaved() {
        Usuario u = new Usuario();
        u.setIdUsuario(10);
        u.setNombre("Ana");
        u.setApellido("Garcia");
        return u;
    }

    private Estudiante estudianteSaved(Usuario u, Grupo g) {
        Estudiante e = new Estudiante();
        e.setIdUsuario(u.getIdUsuario());
        e.setUsuario(u);
        e.setGrupo(g);
        e.setEstActivo(false);
        return e;
    }

    // ── registrar ─────────────────────────────────────────────────────────────

    @Test
    void registrar_datosValidos_retornaEstudiante() {
        Grupo g = grupoMock();
        Usuario uSaved = usuarioSaved();
        Estudiante eSaved = estudianteSaved(uSaved, g);

        when(usuarioRepository.existsByCedula(CI_VALIDA)).thenReturn(false);
        when(grupoRepository.findById(1)).thenReturn(Optional.of(g));
        when(usuarioRepository.existsByUsername(any())).thenReturn(false);
        when(usuarioRepository.existsByCorreo(any())).thenReturn(false);
        when(passwordEncoder.encode("pass1234")).thenReturn("hashed");
        when(usuarioRepository.save(any())).thenReturn(uSaved);
        when(estudianteRepository.save(any())).thenReturn(eSaved);

        Estudiante result = estudianteService.registrar(
                CI_VALIDA, "Ana", "Garcia", "pass1234", MAYOR_EDAD, 1);

        assertThat(result).isNotNull();
        assertThat(result.getEstActivo()).isFalse();
        verify(estudianteRepository).save(any());
        verify(auditoriaService).registrar(eq("CREAR"), eq("estudiantes"), any(), eq(CI_VALIDA));
    }

    @Test
    void registrar_ciInvalida_lanzaIllegalArgument() {
        assertThatThrownBy(() ->
                estudianteService.registrar(CI_INVALIDA, "Ana", "Garcia", "pass1234", MAYOR_EDAD, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CI");

        verifyNoInteractions(usuarioRepository, estudianteRepository);
    }

    @Test
    void registrar_menorDeEdad_lanzaIllegalArgument() {
        assertThatThrownBy(() ->
                estudianteService.registrar(CI_VALIDA, "Ana", "Garcia", "pass1234", MENOR_EDAD, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("18");

        verifyNoInteractions(usuarioRepository, estudianteRepository);
    }

    @Test
    void registrar_passwordCorta_lanzaIllegalArgument() {
        assertThatThrownBy(() ->
                estudianteService.registrar(CI_VALIDA, "Ana", "Garcia", "abc", MAYOR_EDAD, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("contraseña");

        verifyNoInteractions(usuarioRepository, estudianteRepository);
    }

    @Test
    void registrar_cedulaDuplicada_lanzaIllegalArgument() {
        when(usuarioRepository.existsByCedula(CI_VALIDA)).thenReturn(true);

        assertThatThrownBy(() ->
                estudianteService.registrar(CI_VALIDA, "Ana", "Garcia", "pass1234", MAYOR_EDAD, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CI");

        verify(estudianteRepository, never()).save(any());
    }

    @Test
    void registrar_grupoInexistente_lanzaIllegalArgument() {
        when(usuarioRepository.existsByCedula(CI_VALIDA)).thenReturn(false);
        when(grupoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                estudianteService.registrar(CI_VALIDA, "Ana", "Garcia", "pass1234", MAYOR_EDAD, 99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Grupo");

        verify(estudianteRepository, never()).save(any());
    }

    // ── desactivar ────────────────────────────────────────────────────────────

    @Test
    void desactivar_estudianteExistente_setaEstActivo_false() {
        Usuario u = usuarioSaved();
        Grupo g = grupoMock();
        Estudiante e = estudianteSaved(u, g);
        e.setEstActivo(true);

        when(estudianteRepository.findById(10)).thenReturn(Optional.of(e));

        estudianteService.desactivar(10);

        assertThat(e.getEstActivo()).isFalse();
        verify(estudianteRepository).save(e);
        verify(auditoriaService).registrar(eq("BAJA"), eq("estudiantes"), any(), isNull());
    }

    @Test
    void desactivar_estudianteInexistente_lanzaIllegalArgument() {
        when(estudianteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estudianteService.desactivar(99))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
