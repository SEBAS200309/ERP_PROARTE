package com.proarte.erp.eventos.dto;

import com.proarte.erp.eventos.entity.Evento;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventoResponse(
        UUID id,
        UUID cotizacionId,
        String nombre,
        OffsetDateTime fechaInicio,
        OffsetDateTime fechaFin,
        String lugar,
        UUID estadoId,
        Boolean activo,
        UUID createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static EventoResponse from(Evento evento) {
        return new EventoResponse(
                evento.getId(),
                evento.getCotizacionId(),
                evento.getNombre(),
                evento.getFechaInicio(),
                evento.getFechaFin(),
                evento.getLugar(),
                evento.getEstadoId(),
                evento.getActivo(),
                evento.getCreatedBy(),
                evento.getCreatedAt(),
                evento.getUpdatedAt()
        );
    }
}
