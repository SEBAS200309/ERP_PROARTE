package com.proarte.erp.alimentacion.dto;

import com.proarte.erp.alimentacion.entity.EventoAlimentacion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AlimentacionResponse(
        UUID id,
        UUID eventoId,
        String descripcion,
        BigDecimal cantidad,
        String tipoMovimiento,
        OffsetDateTime fecha,
        UUID createdBy
) {

    public static AlimentacionResponse from(EventoAlimentacion alimentacion) {
        return new AlimentacionResponse(
                alimentacion.getId(),
                alimentacion.getEventoId(),
                alimentacion.getDescripcion(),
                alimentacion.getCantidad(),
                alimentacion.getTipoMovimiento(),
                alimentacion.getFecha(),
                alimentacion.getCreatedBy()
        );
    }
}
