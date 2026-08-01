package com.proarte.erp.eventos.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EventoProveedorRequest(
        @NotNull(message = "El proveedor es obligatorio")
        UUID proveedorId,

        UUID servicioId
) {
}
