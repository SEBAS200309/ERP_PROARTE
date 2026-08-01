package com.proarte.erp.eventos.dto;

import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UpdateEventoRequest(
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
        String nombre,

        OffsetDateTime fechaInicio,

        OffsetDateTime fechaFin,

        String lugar,

        UUID estadoId
) {
}
