package com.proarte.erp.catalogos.controller;

import com.proarte.erp.catalogos.dto.CreateCatalogoRequest;
import com.proarte.erp.catalogos.service.CatalogoService;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.security.PermissionEvaluator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/catalogos")
@RequiredArgsConstructor
public class CatalogoController {

    private static final String MODULO = "catalogos";

    private final CatalogoService catalogoService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping("/{tipo}")
    public ResponseEntity<ApiResponse<List<?>>> getAll(
            @PathVariable String tipo,
            @RequestParam(required = false) String contexto) {
        validatePermission("ver_listado");

        List<?> result = catalogoService.getAll(tipo, contexto);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{tipo}/{id}")
    public ResponseEntity<ApiResponse<Object>> getById(
            @PathVariable String tipo,
            @PathVariable UUID id) {
        validatePermission("ver_detalle");

        Object result = catalogoService.getById(tipo, id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{tipo}")
    public ResponseEntity<ApiResponse<Object>> create(
            @PathVariable String tipo,
            @Valid @RequestBody CreateCatalogoRequest request) {
        validatePermission("crear");

        Object result = catalogoService.create(tipo, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Registro de catálogo creado exitosamente"));
    }

    @PutMapping("/{tipo}/{id}")
    public ResponseEntity<ApiResponse<Object>> update(
            @PathVariable String tipo,
            @PathVariable UUID id,
            @Valid @RequestBody CreateCatalogoRequest request) {
        validatePermission("editar");

        Object result = catalogoService.update(tipo, id, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Registro de catálogo actualizado exitosamente"));
    }

    @DeleteMapping("/{tipo}/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable String tipo,
            @PathVariable UUID id) {
        validatePermission("eliminar");

        catalogoService.delete(tipo, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Registro de catálogo eliminado exitosamente"));
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de catálogos");
        }
    }
}
