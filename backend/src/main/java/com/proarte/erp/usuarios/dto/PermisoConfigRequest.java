package com.proarte.erp.usuarios.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record PermisoConfigRequest(
        @NotNull(message = "La configuración de permisos es obligatoria")
        Map<String, Map<String, Boolean>> configuracion
) {
}
