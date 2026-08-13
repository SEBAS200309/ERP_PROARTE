package com.proarte.erp.eventos.controller;

import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.eventos.dto.*;
import com.proarte.erp.eventos.entity.*;
import com.proarte.erp.eventos.service.EventoService;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/eventos")
@RequiredArgsConstructor
public class EventoController {

    private static final String MODULO = "eventos";

    private final EventoService eventoService;
    private final PermissionEvaluator permissionEvaluator;

    // ===================== EVENTO CRUD =====================

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EventoResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID estadoId,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("ver_listado");

        Page<EventoResponse> page = eventoService.getAll(search, estadoId, pageable)
                .map(EventoResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventoResponse>> getById(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        Evento evento = eventoService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(EventoResponse.from(evento)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventoResponse>> create(
            @Valid @RequestBody CreateEventoRequest request) {
        validatePermission("crear");

        Evento evento = eventoService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(EventoResponse.from(evento), "Evento creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventoResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventoRequest request) {
        validatePermission("editar");

        Evento evento = eventoService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(EventoResponse.from(evento), "Evento actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        validatePermission("eliminar");

        eventoService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Evento eliminado exitosamente"));
    }

    @PostMapping("/crear-desde-cotizacion")
    public ResponseEntity<ApiResponse<EventoResponse>> crearDesdeCotizacion(
            @Valid @RequestBody CrearEventoDesdeCotizacionRequest request) {
        validatePermission("crear");

        Evento evento = eventoService.crearDesdeCotizacion(request.cotizacionId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(EventoResponse.from(evento), "Evento creado desde cotización exitosamente"));
    }

    // ===================== CONTACTOS (PERSONAS) =====================

    @GetMapping("/{id}/personas")
    public ResponseEntity<ApiResponse<List<EventoContactoResponse>>> getContactos(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        List<EventoContactoResponse> contactos = eventoService.getContactos(id).stream()
                .map(EventoContactoResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(contactos));
    }

    @PostMapping("/{id}/personas")
    public ResponseEntity<ApiResponse<EventoContactoResponse>> addContacto(
            @PathVariable UUID id,
            @Valid @RequestBody EventoContactoRequest request) {
        validatePermission("editar");

        EventoContacto contacto = eventoService.addContacto(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(EventoContactoResponse.from(contacto), "Contacto asociado al evento exitosamente"));
    }

    @DeleteMapping("/{id}/personas/{contactoId}")
    public ResponseEntity<ApiResponse<Void>> removeContacto(
            @PathVariable UUID id,
            @PathVariable UUID contactoId) {
        validatePermission("editar");

        eventoService.removeContacto(id, contactoId);
        return ResponseEntity.ok(ApiResponse.success(null, "Contacto removido del evento exitosamente"));
    }

    // ===================== PROVEEDORES =====================

    @GetMapping("/{id}/proveedores")
    public ResponseEntity<ApiResponse<List<EventoProveedorResponse>>> getProveedores(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        List<EventoProveedorResponse> proveedores = eventoService.getProveedores(id).stream()
                .map(EventoProveedorResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(proveedores));
    }

    @PostMapping("/{id}/proveedores")
    public ResponseEntity<ApiResponse<EventoProveedorResponse>> addProveedor(
            @PathVariable UUID id,
            @Valid @RequestBody EventoProveedorRequest request) {
        validatePermission("editar");

        EventoProveedor proveedor = eventoService.addProveedor(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(EventoProveedorResponse.from(proveedor), "Proveedor asociado al evento exitosamente"));
    }

    @DeleteMapping("/{id}/proveedores/{proveedorId}")
    public ResponseEntity<ApiResponse<Void>> removeProveedor(
            @PathVariable UUID id,
            @PathVariable UUID proveedorId) {
        validatePermission("editar");

        eventoService.removeProveedor(id, proveedorId);
        return ResponseEntity.ok(ApiResponse.success(null, "Proveedor removido del evento exitosamente"));
    }

    // ===================== OBSERVACIONES =====================

    @GetMapping("/{id}/observaciones")
    public ResponseEntity<ApiResponse<List<ObservacionResponse>>> getObservaciones(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        List<ObservacionResponse> observaciones = eventoService.getObservaciones(id).stream()
                .map(ObservacionResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(observaciones));
    }

    @PostMapping("/{id}/observaciones")
    public ResponseEntity<ApiResponse<ObservacionResponse>> addObservacion(
            @PathVariable UUID id,
            @Valid @RequestBody ObservacionRequest request) {
        validatePermission("editar");

        EventoObservacion observacion = eventoService.addObservacion(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ObservacionResponse.from(observacion), "Observación agregada exitosamente"));
    }

    @PutMapping("/{id}/observaciones/{observacionId}")
    public ResponseEntity<ApiResponse<ObservacionResponse>> updateObservacion(
            @PathVariable UUID id,
            @PathVariable UUID observacionId,
            @Valid @RequestBody ObservacionRequest request) {
        validatePermission("editar");

        EventoObservacion observacion = eventoService.updateObservacion(observacionId, request);
        return ResponseEntity.ok(ApiResponse.success(ObservacionResponse.from(observacion), "Observación actualizada exitosamente"));
    }

    // ===================== INSUMOS =====================

    @GetMapping("/{id}/insumos")
    public ResponseEntity<ApiResponse<List<EventoInsumoResponse>>> getInsumos(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        List<EventoInsumoResponse> insumos = eventoService.getInsumos(id).stream()
                .map(EventoInsumoResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(insumos));
    }

    @PostMapping("/{id}/insumos")
    public ResponseEntity<ApiResponse<EventoInsumoResponse>> addInsumo(
            @PathVariable UUID id,
            @Valid @RequestBody EventoInsumoRequest request) {
        validatePermission("editar");

        EventoInsumo insumo = eventoService.addInsumo(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(EventoInsumoResponse.from(insumo), "Insumo asociado al evento exitosamente"));
    }

    @DeleteMapping("/{id}/insumos/{insumoId}")
    public ResponseEntity<ApiResponse<Void>> removeInsumo(
            @PathVariable UUID id,
            @PathVariable UUID insumoId) {
        validatePermission("editar");

        eventoService.removeInsumo(id, insumoId);
        return ResponseEntity.ok(ApiResponse.success(null, "Insumo removido del evento exitosamente"));
    }

    // ===================== HELPERS =====================

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de eventos");
        }
    }
}
