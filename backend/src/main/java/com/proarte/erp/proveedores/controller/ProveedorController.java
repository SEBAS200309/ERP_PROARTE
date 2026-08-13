package com.proarte.erp.proveedores.controller;

import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.proveedores.dto.*;
import com.proarte.erp.proveedores.entity.Portafolio;
import com.proarte.erp.proveedores.entity.Proveedor;
import com.proarte.erp.proveedores.entity.SolicitudServicio;
import com.proarte.erp.proveedores.service.ProveedorService;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private static final String MODULO = "proveedores";

    private final ProveedorService proveedorService;
    private final PermissionEvaluator permissionEvaluator;

    // ===================== PROVEEDORES =====================

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProveedorResponse>>> getAllProveedores(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("ver_listado");

        Page<ProveedorResponse> page = proveedorService.getAllProveedores(search, pageable)
                .map(ProveedorResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> getProveedorById(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        Proveedor proveedor = proveedorService.getProveedorById(id);
        return ResponseEntity.ok(ApiResponse.success(ProveedorResponse.from(proveedor)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProveedorResponse>> createProveedor(
            @Valid @RequestBody CreateProveedorRequest request) {
        validatePermission("crear");

        Proveedor proveedor = proveedorService.createProveedor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ProveedorResponse.from(proveedor), "Proveedor creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProveedorResponse>> updateProveedor(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProveedorRequest request) {
        validatePermission("editar");

        Proveedor proveedor = proveedorService.updateProveedor(id, request);
        return ResponseEntity.ok(ApiResponse.success(ProveedorResponse.from(proveedor), "Proveedor actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProveedor(@PathVariable UUID id) {
        validatePermission("eliminar");

        proveedorService.deleteProveedor(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Proveedor eliminado exitosamente"));
    }

    // ===================== PORTAFOLIO =====================

    @GetMapping("/{id}/portafolio")
    public ResponseEntity<ApiResponse<List<PortafolioResponse>>> getPortafolio(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        List<PortafolioResponse> portafolio = proveedorService.getPortafolioByProveedor(id)
                .stream()
                .map(PortafolioResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(portafolio));
    }

    @PostMapping("/{id}/portafolio")
    public ResponseEntity<ApiResponse<PortafolioResponse>> createPortafolio(
            @PathVariable UUID id,
            @Valid @RequestBody CreatePortafolioRequest request) {
        validatePermission("crear");

        Portafolio portafolio = proveedorService.createPortafolio(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(PortafolioResponse.from(portafolio), "Portafolio creado exitosamente"));
    }

    @PutMapping("/portafolio/{portafolioId}")
    public ResponseEntity<ApiResponse<PortafolioResponse>> updatePortafolio(
            @PathVariable UUID portafolioId,
            @Valid @RequestBody UpdatePortafolioRequest request) {
        validatePermission("editar");

        Portafolio portafolio = proveedorService.updatePortafolio(portafolioId, request);
        return ResponseEntity.ok(ApiResponse.success(PortafolioResponse.from(portafolio), "Portafolio actualizado exitosamente"));
    }

    @DeleteMapping("/portafolio/{portafolioId}")
    public ResponseEntity<ApiResponse<Void>> deletePortafolio(@PathVariable UUID portafolioId) {
        validatePermission("eliminar");

        proveedorService.deletePortafolio(portafolioId);
        return ResponseEntity.ok(ApiResponse.success(null, "Portafolio eliminado exitosamente"));
    }

    // ===================== SOLICITUDES =====================

    @GetMapping("/solicitudes")
    public ResponseEntity<ApiResponse<PageResponse<SolicitudResponse>>> getAllSolicitudes(
            @RequestParam(required = false) UUID estadoId,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("ver_listado");

        Page<SolicitudResponse> page = proveedorService.getAllSolicitudes(estadoId, pageable)
                .map(SolicitudResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}/solicitudes")
    public ResponseEntity<ApiResponse<PageResponse<SolicitudResponse>>> getSolicitudesByProveedor(
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("ver_detalle");

        Page<SolicitudResponse> page = proveedorService.getSolicitudesByProveedor(id, pageable)
                .map(SolicitudResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<ApiResponse<SolicitudResponse>> createSolicitud(
            @Valid @RequestBody CreateSolicitudRequest request) {
        validatePermission("crear");

        SolicitudServicio solicitud = proveedorService.createSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SolicitudResponse.from(solicitud), "Solicitud de servicio creada exitosamente"));
    }

    @PutMapping("/solicitudes/{solicitudId}")
    public ResponseEntity<ApiResponse<SolicitudResponse>> updateSolicitud(
            @PathVariable UUID solicitudId,
            @Valid @RequestBody UpdateSolicitudRequest request) {
        validatePermission("editar");

        SolicitudServicio solicitud = proveedorService.updateSolicitud(solicitudId, request);
        return ResponseEntity.ok(ApiResponse.success(SolicitudResponse.from(solicitud), "Solicitud actualizada exitosamente"));
    }

    @DeleteMapping("/solicitudes/{solicitudId}")
    public ResponseEntity<ApiResponse<Void>> deleteSolicitud(@PathVariable UUID solicitudId) {
        validatePermission("eliminar");

        proveedorService.deleteSolicitud(solicitudId);
        return ResponseEntity.ok(ApiResponse.success(null, "Solicitud eliminada exitosamente"));
    }

    // ===================== PRIVATE =====================

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de proveedores");
        }
    }
}
