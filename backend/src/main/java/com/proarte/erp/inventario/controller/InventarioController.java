package com.proarte.erp.inventario.controller;

import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.inventario.dto.*;
import com.proarte.erp.inventario.entity.Insumo;
import com.proarte.erp.inventario.entity.InsumoMovimiento;
import com.proarte.erp.inventario.service.InventarioService;
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
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private static final String MODULO = "inventario";

    private final InventarioService inventarioService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InsumoResponse>>> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("ver_listado");

        Page<InsumoResponse> page = inventarioService.getAll(search, pageable)
                .map(InsumoResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InsumoResponse>> getById(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        Insumo insumo = inventarioService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(InsumoResponse.from(insumo)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InsumoResponse>> create(
            @Valid @RequestBody CreateInsumoRequest request) {
        validatePermission("crear");

        Insumo insumo = inventarioService.createInsumo(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(InsumoResponse.from(insumo), "Insumo creado exitosamente"));
    }

    @PostMapping("/ingresos")
    public ResponseEntity<ApiResponse<MovimientoResponse>> registrarIngreso(
            @Valid @RequestBody CreateMovimientoRequest request) {
        validatePermission("crear");

        InsumoMovimiento movimiento = inventarioService.registrarIngreso(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MovimientoResponse.from(movimiento), "Ingreso registrado exitosamente"));
    }

    @PostMapping("/retiros")
    public ResponseEntity<ApiResponse<MovimientoResponse>> registrarRetiro(
            @Valid @RequestBody CreateMovimientoRequest request) {
        validatePermission("crear");

        InsumoMovimiento movimiento = inventarioService.registrarRetiro(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MovimientoResponse.from(movimiento), "Retiro registrado exitosamente"));
    }

    @GetMapping("/{insumoId}/movimientos")
    public ResponseEntity<ApiResponse<PageResponse<MovimientoResponse>>> getMovimientos(
            @PathVariable UUID insumoId,
            @RequestParam(required = false) String tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("ver_detalle");

        Page<MovimientoResponse> page = inventarioService.getMovimientos(insumoId, tipo, pageable)
                .map(MovimientoResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de inventario");
        }
    }
}
