package com.proarte.erp.proveedores.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateProveedorRequest(
        UUID personaId,

        UUID empresaId,

        @Size(max = 100, message = "La especialidad no puede exceder 100 caracteres")
        String especialidad
) {
}
