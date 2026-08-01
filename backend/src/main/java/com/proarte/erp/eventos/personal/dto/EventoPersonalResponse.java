package com.proarte.erp.eventos.personal.dto;

import com.proarte.erp.eventos.personal.entity.EventoPersonal;

import java.math.BigDecimal;
import java.util.UUID;

public record EventoPersonalResponse(
        UUID id,
        UUID eventoId,
        UUID personaId,
        UUID proveedorId,
        UUID servicioId,
        BigDecimal valorTurno,
        Boolean tieneArl,
        Boolean tieneOp,
        String observaciones,
        String alertaArl,
        String alertaOp
) {

    public static EventoPersonalResponse from(EventoPersonal personal) {
        String alertaArl = Boolean.TRUE.equals(personal.getTieneArl())
                ? null
                : "Este empleado no cuenta con ARL vigente";

        String alertaOp = Boolean.TRUE.equals(personal.getTieneOp())
                ? null
                : "Este empleado no cuenta con Orden de Prestación vigente";

        return new EventoPersonalResponse(
                personal.getId(),
                personal.getEventoId(),
                personal.getPersonaId(),
                personal.getProveedorId(),
                personal.getServicioId(),
                personal.getValorTurno(),
                personal.getTieneArl(),
                personal.getTieneOp(),
                personal.getObservaciones(),
                alertaArl,
                alertaOp
        );
    }
}
