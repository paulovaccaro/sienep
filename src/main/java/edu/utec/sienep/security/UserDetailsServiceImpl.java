package edu.utec.sienep.security;

import edu.utec.sienep.entity.Asignacion;
import edu.utec.sienep.entity.Usuario;
import edu.utec.sienep.repository.AsignacionRepository;
import edu.utec.sienep.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final AsignacionRepository asignacionRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        List<SimpleGrantedAuthority> authorities = asignacionRepository
                .findByUsuario_IdUsuarioAndEstActivo(usuario.getIdUsuario(), true)
                .stream()
                .map(Asignacion::getRol)
                .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol.getNombre().replace(" ", "_")))
                .distinct()
                .toList();

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities)
                .accountExpired(!usuario.getEstActivo())
                .credentialsExpired(false)
                .disabled(!usuario.getEstActivo())
                .build();
    }
}
