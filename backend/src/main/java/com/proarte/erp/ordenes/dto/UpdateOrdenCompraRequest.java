package com.proarte.erp.ordenes.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateOrdenCompraRequest(
        UUID solicitudId,
        String descripcion,
        BigDecimal monto,
        UUID estadoId
) {
}
