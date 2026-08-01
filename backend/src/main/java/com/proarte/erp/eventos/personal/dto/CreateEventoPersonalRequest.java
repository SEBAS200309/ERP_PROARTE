package com.proarte.erp.eventos.personal.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateEventoPersonalRequest(
        @NotNull(message = "La persona es obligatoria")
        UUID personaId,

        @NotNull(message = "El proveedor es obligatorio")
        UUID proveedorId,

        UUID servicioId,

        Boolean tieneArl,

        Boolean tieneOp,

        String observaciones
) {
}
