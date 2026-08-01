package com.proarte.erp.eventos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateEventoRequest(
        @NotNull(message = "La cotización es obligatoria")
        UUID cotizacionId,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
        String nombre,

        OffsetDateTime fechaInicio,

        OffsetDateTime fechaFin,

        String lugar,

        UUID estadoId
) {
}
