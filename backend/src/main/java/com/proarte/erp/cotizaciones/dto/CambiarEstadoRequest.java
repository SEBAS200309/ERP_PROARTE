package com.proarte.erp.cotizaciones.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CambiarEstadoRequest(
        @NotNull(message = "El estado es obligatorio")
        UUID estadoId
) {
}
