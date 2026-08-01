package com.proarte.erp.ordenes.controller;

import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.ordenes.dto.CreateOrdenCompraRequest;
import com.proarte.erp.ordenes.dto.OrdenCompraResponse;
import com.proarte.erp.ordenes.dto.UpdateOrdenCompraRequest;
import com.proarte.erp.ordenes.entity.OrdenCompra;
import com.proarte.erp.ordenes.service.OrdenCompraExcelService;
import com.proarte.erp.ordenes.service.OrdenCompraService;
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
@RequestMapping("/api/v1/ordenes-compra")
@RequiredArgsConstructor
public class OrdenCompraController {

    private static final String MODULO = "ordenes-compra";
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final OrdenCompraService ordenCompraService;
    private final OrdenCompraExcelService ordenCompraExcelService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrdenCompraResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID estadoId,
            @RequestParam(required = false) UUID solicitudId,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("leer");

        Page<OrdenCompraResponse> page = ordenCompraService.getAll(search, estadoId, solicitudId, pageable)
                .map(OrdenCompraResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> getById(@PathVariable UUID id) {
        validatePermission("leer");

        OrdenCompra orden = ordenCompraService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(OrdenCompraResponse.from(orden)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> create(
            @Valid @RequestBody CreateOrdenCompraRequest request) {
        validatePermission("crear");

        OrdenCompra orden = ordenCompraService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(OrdenCompraResponse.from(orden), "Orden de compra creada exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrdenCompraResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrdenCompraRequest request) {
        validatePermission("editar");

        OrdenCompra orden = ordenCompraService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(OrdenCompraResponse.from(orden), "Orden de compra actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        validatePermission("eliminar");

        ordenCompraService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Orden de compra eliminada exitosamente"));
    }

    @GetMapping("/descargar-excel")
    public ResponseEntity<byte[]> descargarExcel(
            @RequestParam(required = false) UUID estadoId,
            @RequestParam(required = false) List<UUID> ids) {
        validatePermission("leer");

        List<OrdenCompra> ordenes;
        if (ids != null && !ids.isEmpty()) {
            ordenes = ordenCompraService.findByIds(ids);
        } else {
            Page<OrdenCompra> page = ordenCompraService.getAll(null, estadoId, null, Pageable.unpaged());
            ordenes = page.getContent();
        }

        byte[] excelBytes = ordenCompraExcelService.generateExcel(ordenes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE));
        headers.setContentDispositionFormData("attachment", "ordenes-compra.xlsx");
        headers.setContentLength(excelBytes.length);

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de órdenes de compra");
        }
    }
}
