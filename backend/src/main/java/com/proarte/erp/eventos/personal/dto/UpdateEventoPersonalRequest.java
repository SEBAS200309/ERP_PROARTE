package com.proarte.erp.eventos.personal.dto;

import java.util.UUID;

public record UpdateEventoPersonalRequest(
        UUID servicioId,

        Boolean tieneArl,

        Boolean tieneOp,

        String observaciones
) {
}
