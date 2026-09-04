package com.proarte.erp.leads.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateLeadRequest(
        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        @NotNull(message = "El estado es obligatorio")
        UUID estadoId,

        UUID personaId,

        UUID empresaId
) {
}
