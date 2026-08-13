package com.proarte.erp.cotizaciones.controller;

import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.cotizaciones.dto.*;
import com.proarte.erp.cotizaciones.entity.Cotizacion;
import com.proarte.erp.cotizaciones.service.CotizacionPdfService;
import com.proarte.erp.cotizaciones.service.CotizacionService;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.security.PermissionEvaluator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private static final String MODULO = "cotizaciones";

    private final CotizacionService cotizacionService;
    private final CotizacionPdfService cotizacionPdfService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CotizacionResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID estadoId,
            @RequestParam(required = false) UUID personaId,
            @RequestParam(required = false) UUID empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("ver_listado");

        Page<CotizacionResponse> page = cotizacionService.getAll(search, estadoId, personaId, empresaId, pageable)
                .map(CotizacionResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CotizacionResponse>> getById(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        Cotizacion cotizacion = cotizacionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(CotizacionResponse.from(cotizacion)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CotizacionResponse>> create(
            @Valid @RequestBody CreateCotizacionRequest request) {
        validatePermission("crear");

        Cotizacion cotizacion = cotizacionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(CotizacionResponse.from(cotizacion), "Cotización creada exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CotizacionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCotizacionRequest request) {
        validatePermission("editar");

        Cotizacion cotizacion = cotizacionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(CotizacionResponse.from(cotizacion), "Cotización actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        validatePermission("eliminar");

        cotizacionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cotización eliminada exitosamente"));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<CotizacionResponse>> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody CambiarEstadoRequest request) {
        validatePermission("editar");

        Cotizacion cotizacion = cotizacionService.cambiarEstado(id, request);
        return ResponseEntity.ok(ApiResponse.success(CotizacionResponse.from(cotizacion), "Estado actualizado exitosamente"));
    }

    @GetMapping("/vencimientos")
    public ResponseEntity<ApiResponse<List<CotizacionResponse>>> getPorVencer(
            @RequestParam(required = false, defaultValue = "7") Integer dias) {
        validatePermission("ver_listado");

        List<CotizacionResponse> cotizaciones = cotizacionService.getPorVencer(dias).stream()
                .map(CotizacionResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(cotizaciones));
    }

    @PostMapping("/{id}/recalcular")
    public ResponseEntity<ApiResponse<CotizacionResponse>> recalcularTotal(@PathVariable UUID id) {
        validatePermission("editar");

        cotizacionService.recalcularTotal(id);
        Cotizacion cotizacion = cotizacionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(CotizacionResponse.from(cotizacion), "Total recalculado exitosamente"));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        Cotizacion cotizacion = cotizacionService.getById(id);
        byte[] pdfBytes = cotizacionPdfService.generatePdf(cotizacion);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", cotizacion.getCodigo() + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de cotizaciones");
        }
    }
}
