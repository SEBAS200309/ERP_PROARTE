package com.proarte.erp.roles.dto;

import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdateRolRequest(
        @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
        String nombre,

        String descripcion,

        Map<String, Map<String, Boolean>> configuracion
) {
}
