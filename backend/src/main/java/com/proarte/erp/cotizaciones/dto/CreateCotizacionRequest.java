package com.proarte.erp.cotizaciones.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateCotizacionRequest(
        String codigo,

        @NotNull(message = "El estado es obligatorio")
        UUID estadoId,

        LocalDate fechaVencimiento,

        UUID personaId,

        UUID empresaId,

        @Valid
        List<CotizacionItemRequest> items
) {
}
