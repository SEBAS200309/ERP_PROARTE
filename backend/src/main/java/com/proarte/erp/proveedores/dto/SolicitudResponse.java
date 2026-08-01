package com.proarte.erp.proveedores.dto;

import com.proarte.erp.proveedores.entity.SolicitudServicio;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SolicitudResponse(
        UUID id,
        UUID proveedorId,
        UUID servicioId,
        UUID eventoId,
        UUID estadoId,
        String descripcion,
        Boolean activo,
        UUID createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static SolicitudResponse from(SolicitudServicio solicitud) {
        return new SolicitudResponse(
                solicitud.getId(),
                solicitud.getProveedorId(),
                solicitud.getServicioId(),
                solicitud.getEventoId(),
                solicitud.getEstadoId(),
                solicitud.getDescripcion(),
                solicitud.getActivo(),
                solicitud.getCreatedBy(),
                solicitud.getCreatedAt(),
                solicitud.getUpdatedAt()
        );
    }
}
