package edu.utec.sienep.service;

import edu.utec.sienep.dto.InstanciaResponseDto;
import edu.utec.sienep.entity.CategoriaInstancia;
import edu.utec.sienep.entity.Funcionario;
import edu.utec.sienep.entity.Instancia;
import edu.utec.sienep.repository.CategoriaInstanciaRepository;
import edu.utec.sienep.repository.FuncionarioRepository;
import edu.utec.sienep.repository.InstanciaRepository;
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
    private final GoogleCalendarService googleCalendarService;

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
        if (titulo != null) instancia.setTitulo(titulo);
        if (tipo != null) instancia.setTipo(tipo);
        if (fecHora != null) instancia.setFecHora(fecHora);
        if (descripcion != null) instancia.setDescripcion(descripcion);
        if (idCategoria != null) {
            CategoriaInstancia cat = categoriaRepository.findById(idCategoria)
                    .orElseThrow(() -> new IllegalArgumentException("Categoría de instancia no encontrada: " + idCategoria));
            instancia.setCategoria(cat);
        }
        return instanciaRepository.save(instancia);
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
    }

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
    public InstanciaResponseDto actualizarDto(Integer idInstancia, String titulo, String tipo,
                                              OffsetDateTime fecHora, String descripcion, Integer idCategoria) {
        return InstanciaResponseDto.from(actualizar(idInstancia, titulo, tipo, fecHora, descripcion, idCategoria));
    }

    @Transactional
    public InstanciaResponseDto clonarDto(Integer idInstancia, OffsetDateTime nuevaFecHora) {
        return InstanciaResponseDto.from(clonar(idInstancia, nuevaFecHora));
    }
}
