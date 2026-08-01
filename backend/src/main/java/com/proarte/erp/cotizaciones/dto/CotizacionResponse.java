package com.proarte.erp.cotizaciones.dto;

import com.proarte.erp.cotizaciones.entity.Cotizacion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record CotizacionResponse(
        UUID id,
        String codigo,
        UUID estadoId,
        LocalDate fechaVencimiento,
        BigDecimal total,
        UUID personaId,
        UUID empresaId,
        UUID createdBy,
        Boolean activo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<CotizacionItemResponse> items
) {

    public static CotizacionResponse from(Cotizacion cotizacion) {
        List<CotizacionItemResponse> itemResponses = cotizacion.getItems() != null
                ? cotizacion.getItems().stream().map(CotizacionItemResponse::from).toList()
                : List.of();

        return new CotizacionResponse(
                cotizacion.getId(),
                cotizacion.getCodigo(),
                cotizacion.getEstadoId(),
                cotizacion.getFechaVencimiento(),
                cotizacion.getTotal(),
                cotizacion.getPersonaId(),
                cotizacion.getEmpresaId(),
                cotizacion.getCreatedBy(),
                cotizacion.getActivo(),
                cotizacion.getCreatedAt(),
                cotizacion.getUpdatedAt(),
                itemResponses
        );
    }
}
