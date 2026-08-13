package com.proarte.erp.presentaciones.controller;

import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.presentaciones.dto.CreatePresentacionRequest;
import com.proarte.erp.presentaciones.dto.PresentacionResponse;
import com.proarte.erp.presentaciones.dto.UpdatePresentacionRequest;
import com.proarte.erp.presentaciones.entity.Presentacion;
import com.proarte.erp.presentaciones.service.PresentacionPdfService;
import com.proarte.erp.presentaciones.service.PresentacionService;
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

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/presentaciones")
@RequiredArgsConstructor
public class PresentacionController {

    private static final String MODULO = "presentaciones";

    private final PresentacionService presentacionService;
    private final PresentacionPdfService presentacionPdfService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PresentacionResponse>>> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("ver_listado");

        Page<PresentacionResponse> page = presentacionService.getAll(search, pageable)
                .map(PresentacionResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PresentacionResponse>> getById(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        Presentacion presentacion = presentacionService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(PresentacionResponse.from(presentacion)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PresentacionResponse>> create(
            @Valid @RequestBody CreatePresentacionRequest request) {
        validatePermission("crear");

        Presentacion presentacion = presentacionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(PresentacionResponse.from(presentacion), "Presentación creada exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PresentacionResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePresentacionRequest request) {
        validatePermission("editar");

        Presentacion presentacion = presentacionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(PresentacionResponse.from(presentacion), "Presentación actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        validatePermission("eliminar");

        presentacionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Presentación eliminada exitosamente"));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> generatePdf(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        Presentacion presentacion = presentacionService.getById(id);
        byte[] pdfBytes = presentacionPdfService.generatePdf(presentacion);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "presentacion-" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de presentaciones");
        }
    }
}
