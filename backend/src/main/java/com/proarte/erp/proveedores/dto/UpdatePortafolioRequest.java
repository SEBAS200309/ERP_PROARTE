package com.proarte.erp.proveedores.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdatePortafolioRequest(
        UUID servicioId,

        BigDecimal precioUnitario
) {
}
