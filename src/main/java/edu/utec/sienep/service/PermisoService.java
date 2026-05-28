package edu.utec.sienep.service;

import edu.utec.sienep.repository.AsignacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermisoService {

    private final AsignacionRepository asignacionRepository;

    public boolean tienePermiso(Integer idUsuario, String codigoPermiso, Integer idGrupo) {
        if (idGrupo != null) {
            return asignacionRepository.tienePermisoEnGrupo(idUsuario, codigoPermiso, idGrupo);
        }
        return asignacionRepository.tienePermisoGlobal(idUsuario, codigoPermiso);
    }

    public boolean tienePermisoGlobal(Integer idUsuario, String codigoPermiso) {
        return asignacionRepository.tienePermisoGlobal(idUsuario, codigoPermiso);
    }

    public List<Integer> gruposAccesibles(Integer idUsuario, String codigoPermiso) {
        return asignacionRepository.findGruposAccesibles(idUsuario, codigoPermiso);
    }
}
