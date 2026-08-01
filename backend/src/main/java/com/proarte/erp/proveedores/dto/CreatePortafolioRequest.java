package com.proarte.erp.proveedores.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePortafolioRequest(
        @NotNull(message = "El servicio es obligatorio")
        UUID servicioId,

        BigDecimal precioUnitario
) {
}
