package edu.utec.sienep.service;

import edu.utec.sienep.dto.RolResponseDto;
import edu.utec.sienep.entity.Rol;
import edu.utec.sienep.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<Rol> listarTodos() {
        return rolRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Rol obtenerPorId(Integer id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id));
    }

    @Transactional
    public Rol crear(String nombre, String descripcion) {
        if (rolRepository.findByNombre(nombre).isPresent())
            throw new IllegalStateException("Ya existe un rol con el nombre: " + nombre);
        Rol rol = new Rol();
        rol.setNombre(nombre);
        rol.setDescripcion(descripcion);
        rol.setEsSistema(false);
        rol.setEstActivo(true);
        Rol saved = rolRepository.save(rol);
        auditoriaService.registrar("CREAR", "roles", String.valueOf(saved.getIdRol()), nombre);
        return saved;
    }

    @Transactional
    public Rol actualizar(Integer id, String nombre, String descripcion, Boolean estActivo) {
        Rol rol = obtenerPorId(id);
        if (nombre != null && !nombre.equals(rol.getNombre())) {
            if (rolRepository.findByNombre(nombre).isPresent())
                throw new IllegalStateException("Ya existe un rol con el nombre: " + nombre);
            rol.setNombre(nombre);
        }
        if (descripcion != null) rol.setDescripcion(descripcion);
        if (estActivo != null)   rol.setEstActivo(estActivo);
        Rol saved = rolRepository.save(rol);
        auditoriaService.registrar("MODIFICAR", "roles", String.valueOf(id), saved.getNombre());
        return saved;
    }

    @Transactional
    public void desactivar(Integer id) {
        Rol rol = obtenerPorId(id);
        if (Boolean.TRUE.equals(rol.getEsSistema()))
            throw new IllegalStateException("No se puede desactivar un rol de sistema");
        rol.setEstActivo(false);
        rolRepository.save(rol);
        auditoriaService.registrar("BAJA", "roles", String.valueOf(id), rol.getNombre());
    }

    @Transactional(readOnly = true)
    public List<RolResponseDto> listarTodosDto() {
        return listarTodos().stream().map(RolResponseDto::from).toList();
    }

    @Transactional(readOnly = true)
    public RolResponseDto obtenerDtoPorId(Integer id) {
        return RolResponseDto.from(obtenerPorId(id));
    }

    @Transactional
    public RolResponseDto crearDto(String nombre, String descripcion) {
        return RolResponseDto.from(crear(nombre, descripcion));
    }

    @Transactional
    public RolResponseDto actualizarDto(Integer id, String nombre, String descripcion, Boolean estActivo) {
        return RolResponseDto.from(actualizar(id, nombre, descripcion, estActivo));
    }
}
