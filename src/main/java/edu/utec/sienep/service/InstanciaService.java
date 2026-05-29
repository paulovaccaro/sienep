package edu.utec.sienep.service;

import edu.utec.sienep.dto.InstanciaResponseDto;
import edu.utec.sienep.entity.*;
import edu.utec.sienep.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InstanciaService {

    private final InstanciaRepository instanciaRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final CategoriaInstanciaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PartInstanciaRepository partInstanciaRepository;
    private final GoogleCalendarService googleCalendarService;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;

    @Transactional
    public Instancia crear(String titulo, String tipo, OffsetDateTime fecHora,
                           String descripcion, Integer idFuncionario, Integer idCategoria) {

        Funcionario funcionario = funcionarioRepository.findById(idFuncionario)
                .orElseThrow(() -> new IllegalArgumentException("Funcionario no encontrado: " + idFuncionario));

        Instancia instancia = new Instancia();
        instancia.setTitulo(titulo);
        instancia.setTipo(tipo);
        instancia.setFecHora(fecHora);
        instancia.setDescripcion(descripcion);
        instancia.setEstActivo(true);
        instancia.setFuncionario(funcionario);
        if (idCategoria != null) {
            CategoriaInstancia cat = categoriaRepository.findById(idCategoria)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría de instancia no encontrada: " + idCategoria));
            instancia.setCategoria(cat);
        }
        Instancia saved = instanciaRepository.save(instancia);
        googleCalendarService.crearDesdeInstancia(saved);

        // RF15: notificación automática con el ID generado
        String destinatario = funcionario.getUsuario().getCorreo();
        notificacionService.crearConInstancia(
                saved,
                "Instancia creada – ID: " + saved.getIdInstancia(),
                "Se creó la instancia '" + titulo + "' con identificador: " + saved.getIdInstancia(),
                destinatario
        );

        auditoriaService.registrar("CREAR", "instancias", String.valueOf(saved.getIdInstancia()), titulo);
        return saved;
    }

    /** RF18 – Crea una instancia en el contexto de un estudiante y lo registra como participante. */
    @Transactional
    public Instancia crearDesdeEstudiante(String titulo, String tipo, OffsetDateTime fecHora,
                                          String descripcion, Integer idFuncionario,
                                          Integer idCategoria, Integer idEstudiante) {

        Instancia saved = crear(titulo, tipo, fecHora, descripcion, idFuncionario, idCategoria);

        Usuario participante = usuarioRepository.findById(idEstudiante)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + idEstudiante));

        PartInstancia part = new PartInstancia();
        part.setId(new PartInstanciaId(participante.getIdUsuario(), saved.getIdInstancia()));
        part.setParticipante(participante);
        part.setInstancia(saved);
        partInstanciaRepository.save(part);

        return saved;
    }

    @Transactional(readOnly = true)
    public Instancia obtenerPorId(Integer idInstancia) {
        return instanciaRepository.findById(idInstancia)
                .orElseThrow(() -> new IllegalArgumentException("Instancia no encontrada: " + idInstancia));
    }

    @Transactional(readOnly = true)
    public List<Instancia> listarTodos() {
        return instanciaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Instancia> listarPorFuncionario(Integer idFuncionario) {
        return instanciaRepository.findByFuncionario_IdUsuario(idFuncionario);
    }

    @Transactional
    public Instancia actualizar(Integer idInstancia, String titulo, String tipo,
                                OffsetDateTime fecHora, String descripcion, Integer idCategoria) {
        Instancia instancia = obtenerPorId(idInstancia);
        if (titulo != null)      instancia.setTitulo(titulo);
        if (tipo != null)        instancia.setTipo(tipo);
        if (fecHora != null)     instancia.setFecHora(fecHora);
        if (descripcion != null) instancia.setDescripcion(descripcion);
        if (idCategoria != null) {
            CategoriaInstancia cat = categoriaRepository.findById(idCategoria)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría de instancia no encontrada: " + idCategoria));
            instancia.setCategoria(cat);
        }
        Instancia saved = instanciaRepository.save(instancia);
        auditoriaService.registrar("MODIFICAR", "instancias", String.valueOf(idInstancia), saved.getTitulo());
        return saved;
    }

    @Transactional
    public Instancia clonar(Integer idInstancia, OffsetDateTime nuevaFecHora) {
        Instancia original = obtenerPorId(idInstancia);

        Instancia clon = new Instancia();
        clon.setTitulo(original.getTitulo());
        clon.setTipo(original.getTipo());
        clon.setDescripcion(original.getDescripcion());
        clon.setFuncionario(original.getFuncionario());
        clon.setCategoria(original.getCategoria());
        clon.setFecHora(nuevaFecHora != null ? nuevaFecHora : original.getFecHora());
        clon.setEstActivo(true);
        return instanciaRepository.save(clon);
    }

    @Transactional
    public void desactivar(Integer idInstancia) {
        Instancia instancia = obtenerPorId(idInstancia);
        instancia.setEstActivo(false);
        instanciaRepository.save(instancia);
        auditoriaService.registrar("BAJA", "instancias", String.valueOf(idInstancia), instancia.getTitulo());
    }

    // DTO wrappers

    @Transactional(readOnly = true)
    public List<InstanciaResponseDto> listarTodosDto() {
        return listarTodos().stream().map(InstanciaResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public InstanciaResponseDto obtenerDtoPorId(Integer idInstancia) {
        return InstanciaResponseDto.from(obtenerPorId(idInstancia));
    }

    @Transactional
    public InstanciaResponseDto crearDto(String titulo, String tipo, OffsetDateTime fecHora,
                                         String descripcion, Integer idFuncionario, Integer idCategoria) {
        return InstanciaResponseDto.from(crear(titulo, tipo, fecHora, descripcion, idFuncionario, idCategoria));
    }

    @Transactional
    public InstanciaResponseDto crearDesdeEstudianteDto(String titulo, String tipo, OffsetDateTime fecHora,
                                                         String descripcion, Integer idFuncionario,
                                                         Integer idCategoria, Integer idEstudiante) {
        return InstanciaResponseDto.from(
            crearDesdeEstudiante(titulo, tipo, fecHora, descripcion, idFuncionario, idCategoria, idEstudiante)
        );
    }

    @Transactional
    public InstanciaResponseDto actualizarDto(Integer idInstancia, String titulo, String tipo,
                                              OffsetDateTime fecHora, String descripcion, Integer idCategoria) {
        return InstanciaResponseDto.from(actualizar(idInstancia, titulo, tipo, fecHora, descripcion, idCategoria));
    }

    @Transactional
    public InstanciaResponseDto clonarDto(Integer idInstancia, OffsetDateTime nuevaFecHora) {
        return InstanciaResponseDto.from(clonar(idInstancia, nuevaFecHora));
    }
}
