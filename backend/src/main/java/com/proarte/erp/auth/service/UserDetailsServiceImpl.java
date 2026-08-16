package com.proarte.erp.auth.service;

import com.proarte.erp.auth.entity.Permiso;
import com.proarte.erp.auth.entity.Rol;
import com.proarte.erp.auth.entity.Usuario;
import com.proarte.erp.auth.repository.PermisoRepository;
import com.proarte.erp.auth.repository.UsuarioRepository;
import com.proarte.erp.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PermisoRepository permisoRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado o inactivo: " + username));

        Rol rol = usuario.getRol();
        List<Permiso> permisos = permisoRepository.findByRolId(usuario.getRolId());

        Map<String, Map<String, Boolean>> permisosMap = new HashMap<>();
        for (Permiso permiso : permisos) {
            if (permiso.getConfiguracion() != null) {
                permisosMap.putAll(permiso.getConfiguracion());
            }
        }

        return new CustomUserDetails(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getPasswordHash(),
                usuario.getNombreCompleto(),
                rol.getId(),
                rol.getNombre(),
                permisosMap,
                Boolean.TRUE.equals(usuario.getActivo())
        );
    }
}
