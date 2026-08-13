package com.proarte.erp.servicios.controller;

import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.servicios.dto.*;
import com.proarte.erp.servicios.entity.DescuentoRecargo;
import com.proarte.erp.servicios.service.DescuentoRecargoService;
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

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/descuentos-recargos")
@RequiredArgsConstructor
public class DescuentoRecargoController {

    private static final String MODULO = "descuentos_recargos";

    private final DescuentoRecargoService descuentoRecargoService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DescuentoRecargoResponse>>> getAllDescuentosRecargos(
            @RequestParam(required = false) UUID tipoId,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("ver_listado");

        Page<DescuentoRecargoResponse> page = descuentoRecargoService.getAllDescuentosRecargos(tipoId, pageable)
                .map(DescuentoRecargoResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DescuentoRecargoResponse>> getDescuentoRecargoById(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        DescuentoRecargo descuentoRecargo = descuentoRecargoService.getDescuentoRecargoById(id);
        return ResponseEntity.ok(ApiResponse.success(DescuentoRecargoResponse.from(descuentoRecargo)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DescuentoRecargoResponse>> createDescuentoRecargo(
            @Valid @RequestBody CreateDescuentoRecargoRequest request) {
        validatePermission("crear");

        DescuentoRecargo descuentoRecargo = descuentoRecargoService.createDescuentoRecargo(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(DescuentoRecargoResponse.from(descuentoRecargo), "Descuento/recargo creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DescuentoRecargoResponse>> updateDescuentoRecargo(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDescuentoRecargoRequest request) {
        validatePermission("editar");

        DescuentoRecargo descuentoRecargo = descuentoRecargoService.updateDescuentoRecargo(id, request);
        return ResponseEntity.ok(ApiResponse.success(DescuentoRecargoResponse.from(descuentoRecargo), "Descuento/recargo actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDescuentoRecargo(@PathVariable UUID id) {
        validatePermission("eliminar");

        descuentoRecargoService.deleteDescuentoRecargo(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Descuento/recargo eliminado exitosamente"));
    }

    @PostMapping("/aplicar")
    public ResponseEntity<ApiResponse<Void>> aplicarDescuentoRecargo(
            @Valid @RequestBody AplicarDescuentoRecargoRequest request) {
        validatePermission("crear");

        descuentoRecargoService.aplicar(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Descuento/recargo aplicado exitosamente"));
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de descuentos/recargos");
        }
    }
}
