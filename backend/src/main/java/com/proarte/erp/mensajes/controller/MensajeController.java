package com.proarte.erp.mensajes.controller;

import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.mensajes.dto.CreateMensajeRequest;
import com.proarte.erp.mensajes.dto.MensajeResponse;
import com.proarte.erp.mensajes.dto.UpdateMensajeRequest;
import com.proarte.erp.mensajes.entity.Mensaje;
import com.proarte.erp.mensajes.service.MensajeService;
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
@RequestMapping("/api/v1/mensajes")
@RequiredArgsConstructor
public class MensajeController {

    private static final String MODULO = "mensajes";

    private final MensajeService mensajeService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MensajeResponse>>> getAll(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("leer");

        Page<MensajeResponse> page = mensajeService.getAll(search, pageable)
                .map(MensajeResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MensajeResponse>> getById(@PathVariable UUID id) {
        validatePermission("leer");

        Mensaje mensaje = mensajeService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(MensajeResponse.from(mensaje)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MensajeResponse>> create(
            @Valid @RequestBody CreateMensajeRequest request) {
        validatePermission("crear");

        Mensaje mensaje = mensajeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(MensajeResponse.from(mensaje), "Mensaje creado exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MensajeResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMensajeRequest request) {
        validatePermission("editar");

        Mensaje mensaje = mensajeService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(MensajeResponse.from(mensaje), "Mensaje actualizado exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        validatePermission("eliminar");

        mensajeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Mensaje eliminado exitosamente"));
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de mensajes");
        }
    }
}
