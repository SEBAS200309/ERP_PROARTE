package com.proarte.erp.personas.controller;

import com.proarte.erp.common.dto.PageResponse;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.exception.UnauthorizedException;
import com.proarte.erp.personas.dto.*;
import com.proarte.erp.personas.entity.Persona;
import com.proarte.erp.personas.entity.PersonaEmpresa;
import com.proarte.erp.personas.service.PersonaService;
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

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/personas")
@RequiredArgsConstructor
public class PersonaController {

    private static final String MODULO = "personas";

    private final PersonaService personaService;
    private final PermissionEvaluator permissionEvaluator;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PersonaResponse>>> getAll(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 20) Pageable pageable) {
        validatePermission("ver_listado");

        Page<PersonaResponse> page = personaService.getAll(nombre, documento, email, pageable)
                .map(PersonaResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PersonaResponse>> getById(@PathVariable UUID id) {
        validatePermission("ver_detalle");

        Persona persona = personaService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(PersonaResponse.from(persona)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PersonaResponse>> create(
            @Valid @RequestBody CreatePersonaRequest request) {
        validatePermission("crear");

        Persona persona = personaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(PersonaResponse.from(persona), "Persona creada exitosamente"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PersonaResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePersonaRequest request) {
        validatePermission("editar");

        Persona persona = personaService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(PersonaResponse.from(persona), "Persona actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        validatePermission("eliminar");

        personaService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Persona eliminada exitosamente"));
    }

    @PostMapping("/{id}/asociar-empresa")
    public ResponseEntity<ApiResponse<Map<String, Object>>> asociarEmpresa(
            @PathVariable UUID id,
            @Valid @RequestBody AsociarEmpresaRequest request) {
        validatePermission("editar");

        PersonaEmpresa personaEmpresa = personaService.asociarEmpresa(id, request);
        Map<String, Object> response = Map.of(
                "id", personaEmpresa.getId(),
                "personaId", personaEmpresa.getPersonaId(),
                "empresaId", personaEmpresa.getEmpresaId(),
                "cargo", personaEmpresa.getCargo() != null ? personaEmpresa.getCargo() : ""
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Persona asociada a empresa exitosamente"));
    }

    @PutMapping("/{id}/asignar-rol")
    public ResponseEntity<ApiResponse<PersonaResponse>> asignarRol(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        validatePermission("editar");

        UUID rolEntidadId = UUID.fromString(body.get("rolEntidadId"));
        Persona persona = personaService.asignarRol(id, rolEntidadId);
        return ResponseEntity.ok(ApiResponse.success(PersonaResponse.from(persona), "Rol asignado exitosamente"));
    }

    private void validatePermission(String accion) {
        if (!permissionEvaluator.hasPermission(MODULO, accion)) {
            throw new UnauthorizedException("No tiene permisos para " + accion + " en el módulo de personas");
        }
    }
}
