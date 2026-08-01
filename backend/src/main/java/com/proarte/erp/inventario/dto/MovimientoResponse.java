package com.proarte.erp.inventario.dto;

import com.proarte.erp.inventario.entity.InsumoMovimiento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MovimientoResponse(
        UUID id,
        UUID insumoId,
        String tipoMovimiento,
        BigDecimal cantidad,
        OffsetDateTime fecha,
        String motivo,
        UUID createdBy
) {

    public static MovimientoResponse from(InsumoMovimiento movimiento) {
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getInsumoId(),
                movimiento.getTipoMovimiento(),
                movimiento.getCantidad(),
                movimiento.getFecha(),
                movimiento.getMotivo(),
                movimiento.getCreatedBy()
        );
    }
}
