package com.proarte.erp.roles.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateRolRequest(
        @NotBlank(message = "El nombre del rol es obligatorio")
        @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
        String nombre,

        String descripcion,

        @NotNull(message = "La configuración de permisos es obligatoria")
        Map<String, Map<String, Boolean>> configuracion
) {
}
