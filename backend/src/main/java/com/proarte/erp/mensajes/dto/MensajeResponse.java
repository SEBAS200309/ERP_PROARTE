package com.proarte.erp.mensajes.dto;

import com.proarte.erp.mensajes.entity.Mensaje;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MensajeResponse(
        UUID id,
        String nombre,
        String contenido,
        UUID createdBy,
        Boolean activo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static MensajeResponse from(Mensaje mensaje) {
        return new MensajeResponse(
                mensaje.getId(),
                mensaje.getNombre(),
                mensaje.getContenido(),
                mensaje.getCreatedBy(),
                mensaje.getActivo(),
                mensaje.getCreatedAt(),
                mensaje.getUpdatedAt()
        );
    }
}
