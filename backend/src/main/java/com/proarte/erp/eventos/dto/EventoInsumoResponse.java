package com.proarte.erp.eventos.dto;

import com.proarte.erp.eventos.entity.EventoInsumo;

import java.math.BigDecimal;
import java.util.UUID;

public record EventoInsumoResponse(
        UUID id,
        UUID eventoId,
        UUID insumoId,
        BigDecimal cantidad
) {

    public static EventoInsumoResponse from(EventoInsumo insumo) {
        return new EventoInsumoResponse(
                insumo.getId(),
                insumo.getEventoId(),
                insumo.getInsumoId(),
                insumo.getCantidad()
        );
    }
}
