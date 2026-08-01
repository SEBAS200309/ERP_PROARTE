package com.proarte.erp.servicios.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CategorizarServicioRequest(
        @NotNull(message = "El categoriaId es obligatorio")
        UUID categoriaId
) {
}
