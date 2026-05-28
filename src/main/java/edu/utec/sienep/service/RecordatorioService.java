package edu.utec.sienep.service;

import edu.utec.sienep.dto.InstanciaResponseDto;
import edu.utec.sienep.dto.RecordatorioResponseDto;
import edu.utec.sienep.entity.*;
import edu.utec.sienep.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordatorioService {

    private final RecordatorioRepository recordatorioRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final EstudianteRepository estudianteRepository;
    private final CategoriaRecordatorioRepository categoriaRepository;
    private final InstanciaService instanciaService;
    private final NotificacionService notificacionService;
    private final GoogleCalendarService googleCalendarService;

    @Transactional(readOnly = true)
    public List<Recordatorio> listarTodos() {
        return recordatorioRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public List<Recordatorio> listarPorFuncionario(Integer idFuncionario) {
        return recordatorioRepository.findByFuncionarioWithDetails(idFuncionario);
    }

    @Transactional(readOnly = true)
    public Recordatorio obtenerPorId(Integer id) {
        return recordatorioRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Recordatorio no encontrado: " + id));
    }

    @Transactional
    public Recordatorio crear(String titulo, String descripcion, OffsetDateTime fecHora,
                              Recurrencia recurrencia, Integer idFuncionario,
                              Integer idEstudiante, Integer idCategoria) {

        Funcionario funcionario = funcionarioRepository.findById(idFuncionario)
                .orElseThrow(() -> new IllegalArgumentException("Funcionario no encontrado: " + idFuncionario));

        Estudiante estudiante = idEstudiante != null
                ? estudianteRepository.findById(idEstudiante)
                    .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + idEstudiante))
                : null;

        CategoriaRecordatorio categoria = idCategoria != null
                ? categoriaRepository.findById(idCategoria)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + idCategoria))
                : null;

        Recordatorio r = new Recordatorio();
        r.setTitulo(titulo);
        r.setDescripcion(descripcion);
        r.setFecHora(fecHora);
        r.setRecurrencia(recurrencia != null ? recurrencia : Recurrencia.NINGUNA);
        r.setFuncionario(funcionario);
        r.setEstudiante(estudiante);
        r.setCategoria(categoria);
        r.setEstActivo(true);
        r = recordatorioRepository.save(r);
        Recordatorio saved = recordatorioRepository.findByIdWithDetails(r.getIdRecordatorio()).orElseThrow();
        googleCalendarService.crearDesdeRecordatorio(saved);
        return saved;
    }

    @Transactional
    public Recordatorio actualizar(Integer id, String titulo, String descripcion,
                                   OffsetDateTime fecHora, Recurrencia recurrencia,
                                   Integer idCategoria, Boolean estActivo) {
        Recordatorio r = obtenerPorId(id);
        if (titulo != null)      r.setTitulo(titulo);
        if (descripcion != null) r.setDescripcion(descripcion);
        if (fecHora != null)     r.setFecHora(fecHora);
        if (recurrencia != null) r.setRecurrencia(recurrencia);
        if (estActivo != null)   r.setEstActivo(estActivo);
        if (idCategoria != null) {
            r.setCategoria(categoriaRepository.findById(idCategoria)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada: " + idCategoria)));
        }
        recordatorioRepository.save(r);
        return recordatorioRepository.findByIdWithDetails(r.getIdRecordatorio()).orElseThrow();
    }

    @Transactional
    public void desactivar(Integer id) {
        Recordatorio r = obtenerPorId(id);
        r.setEstActivo(false);
        recordatorioRepository.save(r);
    }

    @Transactional(readOnly = true)
    public List<RecordatorioResponseDto> listarTodosDto() {
        return listarTodos().stream().map(RecordatorioResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<RecordatorioResponseDto> listarPorFuncionarioDto(Integer idFuncionario) {
        return listarPorFuncionario(idFuncionario).stream().map(RecordatorioResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public RecordatorioResponseDto obtenerDtoPorId(Integer id) {
        return RecordatorioResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public RecordatorioResponseDto crearDto(String titulo, String descripcion, OffsetDateTime fecHora,
                                            Recurrencia recurrencia, Integer idFuncionario,
                                            Integer idEstudiante, Integer idCategoria) {
        return RecordatorioResponseDto.from(crear(titulo, descripcion, fecHora, recurrencia,
                idFuncionario, idEstudiante, idCategoria));
    }

    @Transactional
    public RecordatorioResponseDto actualizarDto(Integer id, String titulo, String descripcion,
                                                  OffsetDateTime fecHora, Recurrencia recurrencia,
                                                  Integer idCategoria, Boolean estActivo) {
        return RecordatorioResponseDto.from(actualizar(id, titulo, descripcion, fecHora,
                recurrencia, idCategoria, estActivo));
    }

    @Transactional
    public InstanciaResponseDto convertirInstanciaDto(Integer idRecordatorio) {
        return InstanciaResponseDto.from(convertirInstancia(idRecordatorio));
    }

    /** RF27: convierte un Recordatorio en Instancia y crea la Notificacion asociada */
    @Transactional
    public Instancia convertirInstancia(Integer idRecordatorio) {
        Recordatorio r = obtenerPorId(idRecordatorio);

        if (!r.getEstActivo())
            throw new IllegalStateException("El recordatorio ya está inactivo o fue convertido.");

        Instancia instancia = instanciaService.crear(
                r.getTitulo(),
                "Recordatorio",
                r.getFecHora(),
                r.getDescripcion() != null ? r.getDescripcion() : "",
                r.getFuncionario().getIdUsuario(),
                null
        );

        String destinatario = r.getFuncionario().getUsuario().getCorreo();
        notificacionService.crear(
                instancia.getIdInstancia(),
                "Instancia creada desde recordatorio: " + r.getTitulo(),
                r.getDescripcion() != null ? r.getDescripcion() : r.getTitulo(),
                destinatario
        );

        r.setEstActivo(false);
        recordatorioRepository.save(r);

        return instancia;
    }
}
