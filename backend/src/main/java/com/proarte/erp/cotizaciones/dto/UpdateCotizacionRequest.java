package com.proarte.erp.cotizaciones.dto;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UpdateCotizacionRequest(
        UUID estadoId,
        LocalDate fechaVencimiento,
        UUID personaId,
        UUID empresaId,
        @Valid
        List<CotizacionItemRequest> items
) {
}
