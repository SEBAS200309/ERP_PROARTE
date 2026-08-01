package com.proarte.erp.cotizaciones.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CotizacionItemRequest(
        @NotNull(message = "El servicio es obligatorio")
        UUID servicioId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        Integer cantidad,

        @NotNull(message = "El precio unitario es obligatorio")
        BigDecimal precioUnitario,

        UUID descuentoRecargoId
) {
}
