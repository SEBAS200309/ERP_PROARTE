package com.proarte.erp.empresas.controller;

import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.empresas.dto.CreateEmpresaRequest;
import com.proarte.erp.empresas.dto.EmpresaResponse;
import com.proarte.erp.empresas.dto.UpdateEmpresaRequest;
import com.proarte.erp.empresas.entity.Empresa;
import com.proarte.erp.empresas.service.EmpresaService;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.security.PermissionEvaluator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private static final String MODULO = "empresas";

    private final EmpresaService empresaService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EmpresaResponse>>> getAll(
            @RequestParam(required = false) String razonSocial,
            @RequestParam(required = false) String nit,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("ver_listado");

        Page<EmpresaResponse> page = empresaService.getAll(razonSocial, nit, pageable)
                .map(EmpresaResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponse>> getById(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        Empresa empresa = empresaService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(EmpresaResponse.from(empresa)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmpresaResponse>> create(
            @Valid @RequestBody CreateEmpresaRequest request) {
        validatePermission("crear");

        Empresa empresa = empresaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(EmpresaResponse.from(empresa), "Empresa creada exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpresaResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEmpresaRequest request) {
        validatePermission("editar");

        Empresa empresa = empresaService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(EmpresaResponse.from(empresa), "Empresa actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        validatePermission("eliminar");

        empresaService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Empresa eliminada exitosamente"));
    }

    @PutMapping("/{id}/asignar-rol")
    public ResponseEntity<ApiResponse<EmpresaResponse>> asignarRol(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        validatePermission("editar");

        UUID rolEntidadId = UUID.fromString(body.get("rolEntidadId"));
        Empresa empresa = empresaService.asignarRol(id, rolEntidadId);
        return ResponseEntity.ok(ApiResponse.success(EmpresaResponse.from(empresa), "Rol asignado exitosamente"));
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de empresas");
        }
    }
}
