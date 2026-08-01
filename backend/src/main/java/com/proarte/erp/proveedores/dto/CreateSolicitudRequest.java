package com.proarte.erp.proveedores.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSolicitudRequest(
        @NotNull(message = "El proveedor es obligatorio")
        UUID proveedorId,

        @NotNull(message = "El servicio es obligatorio")
        UUID servicioId,

        UUID eventoId,

        UUID estadoId,

        String descripcion
) {
}
