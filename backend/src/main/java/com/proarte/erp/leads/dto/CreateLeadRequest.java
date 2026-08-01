package com.proarte.erp.leads.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateLeadRequest(
        @NotBlank(message = "La descripción es obligatoria")
        String descripcion,

        UUID estadoId,

        UUID personaId,

        UUID empresaId
) {
}
