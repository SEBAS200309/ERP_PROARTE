package com.proarte.erp.inventario.dto;

import com.proarte.erp.inventario.entity.Insumo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record InsumoResponse(
        UUID id,
        String nombre,
        String descripcion,
        UUID unidadMedidaId,
        BigDecimal stockActual,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static InsumoResponse from(Insumo insumo) {
        return new InsumoResponse(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getDescripcion(),
                insumo.getUnidadMedidaId(),
                insumo.getStockActual(),
                insumo.getCreatedAt(),
                insumo.getUpdatedAt()
        );
    }
}
