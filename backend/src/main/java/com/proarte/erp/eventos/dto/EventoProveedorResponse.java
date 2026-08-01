package com.proarte.erp.eventos.dto;

import com.proarte.erp.eventos.entity.EventoProveedor;

import java.util.UUID;

public record EventoProveedorResponse(
        UUID id,
        UUID eventoId,
        UUID proveedorId,
        UUID servicioId
) {

    public static EventoProveedorResponse from(EventoProveedor proveedor) {
        return new EventoProveedorResponse(
                proveedor.getId(),
                proveedor.getEventoId(),
                proveedor.getProveedorId(),
                proveedor.getServicioId()
        );
    }
}
