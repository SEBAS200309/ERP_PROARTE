package com.proarte.erp.alimentacion.controller;

import com.proarte.erp.alimentacion.dto.AlimentacionResponse;
import com.proarte.erp.alimentacion.dto.CreateAlimentacionRequest;
import com.proarte.erp.alimentacion.entity.EventoAlimentacion;
import com.proarte.erp.alimentacion.service.AlimentacionService;
import com.proarte.erp.common.dto.PageResponse;
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

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/alimentacion")
@RequiredArgsConstructor
public class AlimentacionController {

    private static final String MODULO = "alimentacion";

    private final AlimentacionService alimentacionService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AlimentacionResponse>>> getByEvento(
            @PathVariable UUID eventoId,
            @RequestParam(required = false) String tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("leer");

        Page<AlimentacionResponse> page = alimentacionService.getByEvento(eventoId, tipo, pageable)
                .map(AlimentacionResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @PostMapping("/ingresos")
    public ResponseEntity<ApiResponse<AlimentacionResponse>> registrarIngreso(
            @PathVariable UUID eventoId,
            @Valid @RequestBody CreateAlimentacionRequest request) {
        validatePermission("crear");

        EventoAlimentacion alimentacion = alimentacionService.registrarIngreso(eventoId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AlimentacionResponse.from(alimentacion), "Ingreso registrado exitosamente"));
    }

    @PostMapping("/retiros")
    public ResponseEntity<ApiResponse<AlimentacionResponse>> registrarRetiro(
            @PathVariable UUID eventoId,
            @Valid @RequestBody CreateAlimentacionRequest request) {
        validatePermission("crear");

        EventoAlimentacion alimentacion = alimentacionService.registrarRetiro(eventoId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(AlimentacionResponse.from(alimentacion), "Retiro registrado exitosamente"));
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de alimentación");
        }
    }
}
