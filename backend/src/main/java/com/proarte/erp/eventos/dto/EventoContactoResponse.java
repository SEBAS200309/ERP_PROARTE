package com.proarte.erp.eventos.dto;

import com.proarte.erp.eventos.entity.EventoContacto;

import java.util.UUID;

public record EventoContactoResponse(
        UUID id,
        UUID eventoId,
        UUID personaId,
        UUID rolEventoId,
        String observaciones
) {

    public static EventoContactoResponse from(EventoContacto contacto) {
        return new EventoContactoResponse(
                contacto.getId(),
                contacto.getEventoId(),
                contacto.getPersonaId(),
                contacto.getRolEventoId(),
                contacto.getObservaciones()
        );
    }
}
