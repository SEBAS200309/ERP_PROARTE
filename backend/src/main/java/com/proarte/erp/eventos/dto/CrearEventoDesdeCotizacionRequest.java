package com.proarte.erp.eventos.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CrearEventoDesdeCotizacionRequest(
        @NotNull(message = "La cotización es obligatoria")
        UUID cotizacionId
) {
}
