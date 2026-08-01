package com.proarte.erp.servicios.controller;

import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.servicios.dto.*;
import com.proarte.erp.servicios.entity.Servicio;
import com.proarte.erp.servicios.service.ServicioService;
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
@RequestMapping("/api/v1/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private static final String MODULO = "servicios";

    private final ServicioService servicioService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ServicioResponse>>> getAllServicios(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID categoriaId,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("leer");

        Page<ServicioResponse> page = servicioService.getAllServicios(search, categoriaId, pageable)
                .map(ServicioResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioResponse>> getServicioById(@PathVariable UUID id) {
        validatePermission("leer");

        Servicio servicio = servicioService.getServicioById(id);
        return ResponseEntity.ok(ApiResponse.success(ServicioResponse.from(servicio)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ServicioResponse>> createServicio(
            @Valid @RequestBody CreateServicioRequest request) {
        validatePermission("crear");

        Servicio servicio = servicioService.createServicio(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ServicioResponse.from(servicio), "Servicio creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicioResponse>> updateServicio(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServicioRequest request) {
        validatePermission("editar");

        Servicio servicio = servicioService.updateServicio(id, request);
        return ResponseEntity.ok(ApiResponse.success(ServicioResponse.from(servicio), "Servicio actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteServicio(@PathVariable UUID id) {
        validatePermission("eliminar");

        servicioService.deleteServicio(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Servicio eliminado exitosamente"));
    }

    @GetMapping("/{id}/subservicios")
    public ResponseEntity<ApiResponse<List<ServicioResponse>>> getSubservicios(@PathVariable UUID id) {
        validatePermission("leer");

        List<ServicioResponse> subservicios = servicioService.getSubservicios(id)
                .stream()
                .map(ServicioResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(subservicios));
    }

    @PutMapping("/{id}/categorizar")
    public ResponseEntity<ApiResponse<ServicioResponse>> categorizar(
            @PathVariable UUID id,
            @Valid @RequestBody CategorizarServicioRequest request) {
        validatePermission("editar");

        Servicio servicio = servicioService.categorizar(id, request.categoriaId());
        return ResponseEntity.ok(ApiResponse.success(ServicioResponse.from(servicio), "Servicio categorizado exitosamente"));
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de servicios");
        }
    }
}
