package com.proarte.erp.servicios.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AplicarDescuentoRecargoRequest(
        @NotNull(message = "El descuentoRecargoId es obligatorio")
        UUID descuentoRecargoId,

        UUID servicioId,

        UUID personaId,

        UUID empresaId
) {
}
