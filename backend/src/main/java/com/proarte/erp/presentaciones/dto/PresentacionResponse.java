package com.proarte.erp.presentaciones.dto;

import com.proarte.erp.presentaciones.entity.Presentacion;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PresentacionResponse(
        UUID id,
        String nombre,
        String descripcion,
        UUID servicioId,
        UUID createdBy,
        Boolean activo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static PresentacionResponse from(Presentacion presentacion) {
        return new PresentacionResponse(
                presentacion.getId(),
                presentacion.getNombre(),
                presentacion.getDescripcion(),
                presentacion.getServicioId(),
                presentacion.getCreatedBy(),
                presentacion.getActivo(),
                presentacion.getCreatedAt(),
                presentacion.getUpdatedAt()
        );
    }
}
