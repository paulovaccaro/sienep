package edu.utec.sienep;

import edu.utec.sienep.dto.InstanciaResponseDto;
import edu.utec.sienep.entity.*;
import edu.utec.sienep.repository.*;
import edu.utec.sienep.service.AuditoriaService;
import edu.utec.sienep.service.GoogleCalendarService;
import edu.utec.sienep.service.InstanciaService;
import edu.utec.sienep.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para InstanciaService (RF10, RF14, RF15, RF16, RF17).
 */
@ExtendWith(MockitoExtension.class)
class InstanciaServiceTest {

    @Mock InstanciaRepository instanciaRepository;
    @Mock FuncionarioRepository funcionarioRepository;
    @Mock CategoriaInstanciaRepository categoriaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PartInstanciaRepository partInstanciaRepository;
    @Mock GoogleCalendarService googleCalendarService;
    @Mock NotificacionService notificacionService;
    @Mock AuditoriaService auditoriaService;

    @InjectMocks InstanciaService instanciaService;

    private static final OffsetDateTime FEC_HORA =
            OffsetDateTime.of(2025, 9, 1, 10, 0, 0, 0, ZoneOffset.ofHours(-3));

    private Funcionario funcionarioMock(int id) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setNombre("Juan");
        u.setApellido("Perez");
        u.setCorreo("juan.perez@utec.edu.uy");

        Funcionario f = new Funcionario();
        f.setIdUsuario(id);
        f.setUsuario(u);
        f.setEstActivo(true);
        return f;
    }

    private Instancia instanciaMock(int id, Funcionario f) {
        Instancia i = new Instancia();
        i.setIdInstancia(id);
        i.setTitulo("Reunión test");
        i.setTipo("Seguimiento");
        i.setFecHora(FEC_HORA);
        i.setDescripcion("Descripción test");
        i.setEstActivo(true);
        i.setFuncionario(f);
        return i;
    }

    // ── crear (RF10, RF14, RF15) ──────────────────────────────────────────────

    @Test
    void crear_funcionarioExistente_retornaInstancia() {
        Funcionario f = funcionarioMock(1);
        Instancia saved = instanciaMock(42, f);

        when(funcionarioRepository.findById(1)).thenReturn(Optional.of(f));
        when(instanciaRepository.save(any())).thenReturn(saved);

        Instancia result = instanciaService.crear(
                "Reunión test", "Seguimiento", FEC_HORA, "Descripción test", 1, null);

        assertThat(result.getIdInstancia()).isEqualTo(42);
        assertThat(result.getTitulo()).isEqualTo("Reunión test");
        // RF15: se crea notificación automática
        verify(notificacionService).crearConInstancia(eq(saved), contains("42"), any(), eq("juan.perez@utec.edu.uy"));
        verify(auditoriaService).registrar(eq("CREAR"), eq("instancias"), eq("42"), eq("Reunión test"));
    }

    @Test
    void crear_funcionarioInexistente_lanzaIllegalArgument() {
        when(funcionarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                instanciaService.crear("Título", "Tipo", FEC_HORA, "Desc", 99, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Funcionario");

        verify(instanciaRepository, never()).save(any());
    }

    // ── crearDto ─────────────────────────────────────────────────────────────

    @Test
    void crearDto_retornaResponseDto() {
        Funcionario f = funcionarioMock(1);
        Instancia saved = instanciaMock(10, f);

        when(funcionarioRepository.findById(1)).thenReturn(Optional.of(f));
        when(instanciaRepository.save(any())).thenReturn(saved);

        InstanciaResponseDto dto = instanciaService.crearDto(
                "Reunión test", "Seguimiento", FEC_HORA, "Desc", 1, null);

        assertThat(dto.getIdInstancia()).isEqualTo(10);
        assertThat(dto.getTitulo()).isEqualTo("Reunión test");
        assertThat(dto.getIdFuncionario()).isEqualTo(1);
    }

    // ── desactivar (RF16) ─────────────────────────────────────────────────────

    @Test
    void desactivar_instanciaExistente_setaEstActivo_false() {
        Funcionario f = funcionarioMock(1);
        Instancia inst = instanciaMock(5, f);
        inst.setEstActivo(true);

        when(instanciaRepository.findById(5)).thenReturn(Optional.of(inst));

        instanciaService.desactivar(5);

        assertThat(inst.getEstActivo()).isFalse();
        verify(instanciaRepository).save(inst);
        verify(auditoriaService).registrar(eq("BAJA"), eq("instancias"), eq("5"), any());
    }

    @Test
    void desactivar_instanciaInexistente_lanzaIllegalArgument() {
        when(instanciaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> instanciaService.desactivar(99))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── actualizar (RF16) ─────────────────────────────────────────────────────

    @Test
    void actualizar_cambiaTitulo() {
        Funcionario f = funcionarioMock(1);
        Instancia inst = instanciaMock(7, f);

        when(instanciaRepository.findById(7)).thenReturn(Optional.of(inst));
        when(instanciaRepository.save(any())).thenReturn(inst);

        instanciaService.actualizar(7, "Nuevo Título", null, null, null, null);

        assertThat(inst.getTitulo()).isEqualTo("Nuevo Título");
        verify(auditoriaService).registrar(eq("MODIFICAR"), eq("instancias"), eq("7"), any());
    }

    // ── listar ────────────────────────────────────────────────────────────────

    @Test
    void listarTodos_retornaListaDtos() {
        Funcionario f = funcionarioMock(1);
        Instancia i1 = instanciaMock(1, f);
        Instancia i2 = instanciaMock(2, f);

        when(instanciaRepository.findAll()).thenReturn(List.of(i1, i2));

        List<InstanciaResponseDto> result = instanciaService.listarTodosDto();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(InstanciaResponseDto::getIdInstancia)
                .containsExactlyInAnyOrder(1, 2);
    }
}
