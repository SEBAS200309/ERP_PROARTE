package com.proarte.erp.inventario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateMovimientoRequest(
        @NotNull(message = "El insumo es obligatorio")
        UUID insumoId,
        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a cero")
        BigDecimal cantidad,
        String motivo
) {}
