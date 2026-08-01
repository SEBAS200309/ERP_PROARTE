package com.proarte.erp.ordenes.dto;

import com.proarte.erp.ordenes.entity.OrdenCompra;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenCompraResponse(
        UUID id,
        String codigo,
        UUID solicitudId,
        String descripcion,
        BigDecimal monto,
        UUID estadoId,
        UUID createdBy,
        Boolean activo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static OrdenCompraResponse from(OrdenCompra orden) {
        return new OrdenCompraResponse(
                orden.getId(),
                orden.getCodigo(),
                orden.getSolicitudId(),
                orden.getDescripcion(),
                orden.getMonto(),
                orden.getEstadoId(),
                orden.getCreatedBy(),
                orden.getActivo(),
                orden.getCreatedAt(),
                orden.getUpdatedAt()
        );
    }
}
