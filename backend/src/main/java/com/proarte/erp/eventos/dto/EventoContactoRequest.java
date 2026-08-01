package com.proarte.erp.eventos.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EventoContactoRequest(
        @NotNull(message = "La persona es obligatoria")
        UUID personaId,

        UUID rolEventoId,

        String observaciones
) {
}
