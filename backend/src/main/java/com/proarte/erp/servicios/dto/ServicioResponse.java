package com.proarte.erp.servicios.dto;

import com.proarte.erp.servicios.entity.Servicio;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ServicioResponse(
        UUID id,
        String nombre,
        String descripcion,
        Boolean esPropio,
        Boolean requiereOc,
        UUID servicioPadreId,
        UUID categoriaId,
        Boolean activo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static ServicioResponse from(Servicio servicio) {
        return new ServicioResponse(
                servicio.getId(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getEsPropio(),
                servicio.getRequiereOc(),
                servicio.getServicioPadreId(),
                servicio.getCategoriaId(),
                servicio.getActivo(),
                servicio.getCreatedAt(),
                servicio.getUpdatedAt()
        );
    }
}
