package com.proarte.erp.proveedores.dto;

import java.util.UUID;

public record UpdateSolicitudRequest(
        UUID proveedorId,

        UUID servicioId,

        UUID eventoId,

        UUID estadoId,

        String descripcion
) {
}
