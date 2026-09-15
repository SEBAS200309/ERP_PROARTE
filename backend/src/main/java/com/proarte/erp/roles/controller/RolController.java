package com.proarte.erp.roles.controller;

import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.roles.dto.CreateRolRequest;
import com.proarte.erp.roles.dto.RolResponse;
import com.proarte.erp.roles.dto.UpdateRolRequest;
import com.proarte.erp.roles.service.RolService;
import com.proarte.erp.security.PermissionEvaluator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RolController {

    private static final String MODULO = "roles";

    private final RolService rolService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RolResponse>>> getAll() {
        validatePermission("leer");
        return ResponseEntity.ok(ApiResponse.success(rolService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RolResponse>> getById(@PathVariable UUID id) {
        validatePermission("leer");
        return ResponseEntity.ok(ApiResponse.success(rolService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RolResponse>> create(@Valid @RequestBody CreateRolRequest request) {
        validatePermission("crear");
        RolResponse response = rolService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Rol creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RolResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRolRequest request) {
        validatePermission("editar");
        RolResponse response = rolService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Rol actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        validatePermission("eliminar");
        rolService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Rol eliminado exitosamente"));
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de roles");
        }
    }
}
