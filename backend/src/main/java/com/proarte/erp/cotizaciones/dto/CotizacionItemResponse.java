package com.proarte.erp.cotizaciones.dto;

import com.proarte.erp.cotizaciones.entity.CotizacionItem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CotizacionItemResponse(
        UUID id,
        UUID cotizacionId,
        UUID servicioId,
        Integer cantidad,
        BigDecimal precioUnitario,
        UUID descuentoRecargoId,
        BigDecimal subtotal,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static CotizacionItemResponse from(CotizacionItem item) {
        return new CotizacionItemResponse(
                item.getId(),
                item.getCotizacionId(),
                item.getServicioId(),
                item.getCantidad(),
                item.getPrecioUnitario(),
                item.getDescuentoRecargoId(),
                item.getSubtotal(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
