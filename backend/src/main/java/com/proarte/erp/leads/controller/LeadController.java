package com.proarte.erp.leads.controller;

import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.leads.dto.*;
import com.proarte.erp.leads.entity.Lead;
import com.proarte.erp.leads.service.LeadService;
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
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
public class LeadController {

    private static final String MODULO = "leads";

    private final LeadService leadService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LeadResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID estadoId,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("leer");

        Page<LeadResponse> page = leadService.getAll(search, estadoId, pageable)
                .map(LeadResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> getById(@PathVariable UUID id) {
        validatePermission("leer");

        Lead lead = leadService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(LeadResponse.from(lead)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LeadResponse>> create(
            @Valid @RequestBody CreateLeadRequest request) {
        validatePermission("crear");

        Lead lead = leadService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(LeadResponse.from(lead), "Lead creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLeadRequest request) {
        validatePermission("editar");

        Lead lead = leadService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(LeadResponse.from(lead), "Lead actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        validatePermission("eliminar");

        leadService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Lead eliminado exitosamente"));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<ApiResponse<LeadEstadisticasResponse>> getEstadisticas() {
        validatePermission("leer");

        Map<String, Long> estadisticas = leadService.getEstadisticas();
        return ResponseEntity.ok(ApiResponse.success(new LeadEstadisticasResponse(estadisticas)));
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de leads");
        }
    }
}
