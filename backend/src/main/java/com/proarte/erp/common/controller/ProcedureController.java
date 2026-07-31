package com.proarte.erp.common.controller;

import com.proarte.erp.common.dto.ProcedureRequest;
import com.proarte.erp.common.service.ProcedureExecutorService;
import com.proarte.erp.exception.ApiResponse;
import com.proarte.erp.security.PermissionEvaluator;
import com.proarte.erp.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/{modulo}/execute")
@RequiredArgsConstructor
public class ProcedureController {

    private final ProcedureExecutorService procedureExecutorService;
    private final PermissionEvaluator permissionEvaluator;

    @PostMapping("/{functionName}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> executeFunction(
            @PathVariable String modulo,
            @PathVariable String functionName,
            @RequestBody ProcedureRequest request
    ) {
        log.info("Solicitud de ejecucion: modulo={}, funcion={}", modulo, functionName);

        // Validar permisos del usuario sobre el módulo
        if (!permissionEvaluator.hasPermission(modulo, "ejecutar")) {
            log.warn("Permiso denegado para ejecutar funcion en modulo={}, funcion={}", modulo, functionName);
            throw new UnauthorizedException("No tiene permisos para ejecutar funciones en el modulo: " + modulo);
        }

        Map<String, Object> result = procedureExecutorService.executeFunction(
                functionName,
                request.params()
        );

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
