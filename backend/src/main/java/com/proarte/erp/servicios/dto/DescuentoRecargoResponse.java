package com.proarte.erp.servicios.dto;

import com.proarte.erp.servicios.entity.DescuentoRecargo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DescuentoRecargoResponse(
        UUID id,
        String nombre,
        BigDecimal valor,
        UUID tipoId,
        Boolean activo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static DescuentoRecargoResponse from(DescuentoRecargo descuentoRecargo) {
        return new DescuentoRecargoResponse(
                descuentoRecargo.getId(),
                descuentoRecargo.getNombre(),
                descuentoRecargo.getValor(),
                descuentoRecargo.getTipoId(),
                descuentoRecargo.getActivo(),
                descuentoRecargo.getCreatedAt(),
                descuentoRecargo.getUpdatedAt()
        );
    }
}
