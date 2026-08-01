package com.proarte.erp.ordenes.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrdenCompraRequest(
        String codigo,

        @NotNull(message = "La solicitud de servicio es obligatoria")
        UUID solicitudId,

        String descripcion,

        BigDecimal monto,

        @NotNull(message = "El estado es obligatorio")
        UUID estadoId
) {
}
