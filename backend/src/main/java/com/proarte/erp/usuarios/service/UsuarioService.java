package com.proarte.erp.usuarios.service;

import com.proarte.erp.auth.entity.Permiso;
import com.proarte.erp.auth.entity.Usuario;
import com.proarte.erp.auth.repository.PermisoRepository;
import com.proarte.erp.auth.repository.RolRepository;
import com.proarte.erp.auth.repository.UsuarioRepository;
import com.proarte.erp.exception.BusinessException;
import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.usuarios.dto.CreateUsuarioRequest;
import com.proarte.erp.usuarios.dto.UpdateUsuarioRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<Usuario> getAll(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Usuario getById(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }

    @Transactional
    public Usuario create(CreateUsuarioRequest request) {
        if (usuarioRepository.findByUsername(request.username()).isPresent()) {
            throw new BusinessException("El nombre de usuario '" + request.username() + "' ya está en uso");
        }

        if (!rolRepository.existsActiveById(request.rolId())) {
            throw new ResourceNotFoundException("Rol", "id", request.rolId());
        }

        Usuario usuario = Usuario.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .nombreCompleto(request.nombreCompleto())
                .email(request.email())
                .rolId(request.rolId())
                .build();
        usuario.setActivo(true);

        Usuario saved = usuarioRepository.save(usuario);
        log.info("Usuario creado: id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    @Transactional
    public Usuario update(UUID id, UpdateUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        if (request.nombreCompleto() != null) {
            usuario.setNombreCompleto(request.nombreCompleto());
        }
        if (request.email() != null) {
            usuario.setEmail(request.email());
        }
        if (request.rolId() != null) {
            if (!rolRepository.existsActiveById(request.rolId())) {
                throw new ResourceNotFoundException("Rol", "id", request.rolId());
            }
            usuario.setRolId(request.rolId());
        }
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        Usuario updated = usuarioRepository.save(usuario);
        log.info("Usuario actualizado: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void delete(UUID id) {
        if (!usuarioRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("Usuario", "id", id);
        }
        usuarioRepository.softDelete(id);
        log.info("Usuario eliminado (soft-delete): id={}", id);
    }

    @Transactional(readOnly = true)
    public List<Permiso> getPermisosByRolId(UUID rolId) {
        if (!rolRepository.existsActiveById(rolId)) {
            throw new ResourceNotFoundException("Rol", "id", rolId);
        }
        return permisoRepository.findByRolId(rolId);
    }

    @Transactional
    public Permiso updatePermisosForRol(UUID rolId, Map<String, Map<String, Boolean>> configuracion) {
        if (!rolRepository.existsActiveById(rolId)) {
            throw new ResourceNotFoundException("Rol", "id", rolId);
        }

        List<Permiso> existingPermisos = permisoRepository.findByRolId(rolId);

        Permiso permiso;
        if (existingPermisos.isEmpty()) {
            permiso = Permiso.builder()
                    .rolId(rolId)
                    .configuracion(configuracion)
                    .build();
            permiso.setActivo(true);
        } else {
            permiso = existingPermisos.get(0);
            permiso.setConfiguracion(configuracion);
        }

        Permiso saved = permisoRepository.save(permiso);
        log.info("Permisos actualizados para rol: rolId={}", rolId);
        return saved;
    }
}
