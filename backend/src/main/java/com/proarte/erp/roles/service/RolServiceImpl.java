package com.proarte.erp.roles.service;

import com.proarte.erp.auth.repository.UsuarioRepository;
import com.proarte.erp.exception.BusinessException;
import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.roles.dto.CreateRolRequest;
import com.proarte.erp.roles.dto.RolResponse;
import com.proarte.erp.roles.dto.UpdateRolRequest;
import com.proarte.erp.roles.entity.Permiso;
import com.proarte.erp.roles.entity.Rol;
import com.proarte.erp.roles.repository.PermisoRepository;
import com.proarte.erp.roles.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RolResponse> getAll() {
        List<Rol> roles = rolRepository.findAll();
        List<Permiso> permisos = permisoRepository.findAll();

        Map<UUID, Permiso> permisosByRolId = permisos.stream()
                .collect(Collectors.toMap(
                        Permiso::getRolId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        return roles.stream()
                .map(rol -> RolResponse.of(rol, permisosByRolId.get(rol.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RolResponse getById(UUID id) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", id));

        Permiso permiso = permisoRepository.findByRolId(id).orElse(null);
        return RolResponse.of(rol, permiso);
    }

    @Override
    @Transactional
    public RolResponse create(CreateRolRequest request) {
        if (rolRepository.existsByNombreIgnoreCaseAndActivoTrue(request.nombre())) {
            throw new BusinessException("Ya existe un rol activo con el nombre: " + request.nombre());
        }

        Rol rol = Rol.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .build();
        rol.setActivo(true);
        Rol savedRol = rolRepository.save(rol);

        Permiso permiso = Permiso.builder()
                .rolId(savedRol.getId())
                .configuracion(request.configuracion() != null ? request.configuracion() : Collections.emptyMap())
                .build();
        permiso.setActivo(true);
        Permiso savedPermiso = permisoRepository.save(permiso);

        log.info("Rol y permisos creados atómicamente: id={}, nombre={}", savedRol.getId(), savedRol.getNombre());
        return RolResponse.of(savedRol, savedPermiso);
    }

    @Override
    @Transactional
    public RolResponse update(UUID id, UpdateRolRequest request) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", id));

        if (request.nombre() != null && !request.nombre().isBlank()) {
            rolRepository.findByNombre(request.nombre()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BusinessException("Ya existe otro rol con el nombre: " + request.nombre());
                }
            });
            rol.setNombre(request.nombre());
        }

        if (request.descripcion() != null) {
            rol.setDescripcion(request.descripcion());
        }

        Rol updatedRol = rolRepository.save(rol);

        Permiso permiso;
        if (request.configuracion() != null) {
            permiso = permisoRepository.findByRolId(id).orElseGet(() -> {
                Permiso p = Permiso.builder()
                        .rolId(id)
                        .build();
                p.setActivo(true);
                return p;
            });
            permiso.setConfiguracion(request.configuracion());
            permiso = permisoRepository.save(permiso);
        } else {
            permiso = permisoRepository.findByRolId(id).orElse(null);
        }

        log.info("Rol y configuración de permisos actualizados: id={}", updatedRol.getId());
        return RolResponse.of(updatedRol, permiso);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        if (!rolRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("Rol", "id", id);
        }

        if (usuarioRepository.existsByRolIdAndActivoTrue(id)) {
            throw new BusinessException("No se puede eliminar el rol porque tiene usuarios activos asignados");
        }

        permisoRepository.softDeleteByRolId(id);
        rolRepository.softDelete(id);
        log.info("Rol y permisos eliminados (soft-delete): id={}", id);
    }
}
