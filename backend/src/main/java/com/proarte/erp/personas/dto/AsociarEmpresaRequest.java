package com.proarte.erp.personas.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AsociarEmpresaRequest(
        @NotNull(message = "El ID de empresa es obligatorio")
        UUID empresaId,

        String cargo
) {
}
