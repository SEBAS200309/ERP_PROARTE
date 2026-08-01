package com.proarte.erp.eventos.dto;

import com.proarte.erp.eventos.entity.EventoObservacion;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ObservacionResponse(
        UUID id,
        UUID eventoId,
        String texto,
        OffsetDateTime fecha,
        UUID createdBy
) {

    public static ObservacionResponse from(EventoObservacion observacion) {
        return new ObservacionResponse(
                observacion.getId(),
                observacion.getEventoId(),
                observacion.getTexto(),
                observacion.getFecha(),
                observacion.getCreatedBy()
        );
    }
}
