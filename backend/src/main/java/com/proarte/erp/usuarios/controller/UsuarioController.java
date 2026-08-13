package com.proarte.erp.usuarios.controller;

import com.proarte.erp.auth.entity.Permiso;
import com.proarte.erp.auth.entity.Usuario;
import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.security.PermissionEvaluator;
import com.proarte.erp.usuarios.dto.*;
import com.proarte.erp.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private static final String MODULO = "usuarios";

    private final UsuarioService usuarioService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UsuarioResponse>>> getAll(
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("ver_listado");

        Page<UsuarioResponse> page = usuarioService.getAll(pageable)
                .map(UsuarioResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> getById(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        Usuario usuario = usuarioService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(UsuarioResponse.from(usuario)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioResponse>> create(
            @Valid @RequestBody CreateUsuarioRequest request) {
        validatePermission("crear");

        Usuario usuario = usuarioService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(UsuarioResponse.from(usuario), "Usuario creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUsuarioRequest request) {
        validatePermission("editar");

        Usuario usuario = usuarioService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(UsuarioResponse.from(usuario), "Usuario actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        validatePermission("eliminar");

        usuarioService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Usuario eliminado exitosamente"));
    }

    @GetMapping("/roles/{id}/permisos")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPermisosByRol(
            @PathVariable UUID id) {
        validatePermission("ver_detalle");

        List<Permiso> permisos = usuarioService.getPermisosByRolId(id);

        Map<String, Map<String, Boolean>> tablas = permisos.stream()
                .filter(p -> p.getConfiguracion() != null)
                .flatMap(p -> p.getConfiguracion().entrySet().stream())
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v2
                ));

        Map<String, Object> response = Map.of(
                "tablas", tablas,
                "contexto", Map.of()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/roles/{id}/permisos")
    public ResponseEntity<ApiResponse<Map<String, Map<String, Boolean>>>> updatePermisosByRol(
            @PathVariable UUID id,
            @Valid @RequestBody PermisoConfigRequest request) {
        validatePermission("editar");

        Permiso permiso = usuarioService.updatePermisosForRol(id, request.configuracion());
        return ResponseEntity.ok(ApiResponse.success(permiso.getConfiguracion(), "Permisos actualizados exitosamente"));
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de usuarios");
        }
    }
}
