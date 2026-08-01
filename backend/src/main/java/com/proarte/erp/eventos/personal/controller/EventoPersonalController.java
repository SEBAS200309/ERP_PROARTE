package com.proarte.erp.eventos.personal.controller;

import com.proarte.erp.common.dto.ProcedureRequest;
import com.proarte.erp.eventos.personal.dto.*;
import com.proarte.erp.eventos.personal.entity.EventoPersonal;
import com.proarte.erp.eventos.personal.service.EventoPersonalService;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.security.PermissionEvaluator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/personal")
@RequiredArgsConstructor
public class EventoPersonalController {

    private static final String MODULO = "evento_personal";

    private final EventoPersonalService eventoPersonalService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventoPersonalResponse>>> getAll(@PathVariable UUID eventoId) {
        validatePermission("leer");

        List<EventoPersonalResponse> personal = eventoPersonalService.getByEventoId(eventoId).stream()
                .map(EventoPersonalResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(personal));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventoPersonalResponse>> create(
            @PathVariable UUID eventoId,
            @Valid @RequestBody CreateEventoPersonalRequest request) {
        validatePermission("crear");

        EventoPersonal personal = eventoPersonalService.create(eventoId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(EventoPersonalResponse.from(personal), "Personal agregado al evento exitosamente"));
    }

    @PutMapping("/{personalId}")
    public ResponseEntity<ApiResponse<EventoPersonalResponse>> update(
            @PathVariable UUID eventoId,
            @PathVariable UUID personalId,
            @Valid @RequestBody UpdateEventoPersonalRequest request) {
        validatePermission("editar");

        EventoPersonal personal = eventoPersonalService.update(eventoId, personalId, request);
        return ResponseEntity.ok(ApiResponse.success(EventoPersonalResponse.from(personal), "Personal actualizado exitosamente"));
    }

    @DeleteMapping("/{personalId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID eventoId,
            @PathVariable UUID personalId) {
        validatePermission("eliminar");

        eventoPersonalService.delete(eventoId, personalId);
        return ResponseEntity.ok(ApiResponse.success(null, "Personal removido del evento exitosamente"));
    }

    @PostMapping("/execute/calcular_turno")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calcularValorTurno(
            @PathVariable UUID eventoId,
            @RequestBody ProcedureRequest request) {
        validatePermission("ejecutar");

        Object eventoPersonalIdParam = request.params().get("evento_personal_id");
        UUID eventoPersonalId = UUID.fromString(eventoPersonalIdParam.toString());

        BigDecimal valor = eventoPersonalService.calcularValorTurno(eventoPersonalId);

        Map<String, Object> result = Map.of("valor_turno", valor);
        return ResponseEntity.ok(ApiResponse.success(result, "Valor del turno calculado exitosamente"));
    }

    // ===================== HELPERS =====================

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de personal de evento");
        }
    }
}
