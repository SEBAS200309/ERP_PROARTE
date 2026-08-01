package com.proarte.erp.proveedores.dto;

import com.proarte.erp.proveedores.entity.Portafolio;

import java.math.BigDecimal;
import java.util.UUID;

public record PortafolioResponse(
        UUID id,
        UUID proveedorId,
        UUID servicioId,
        BigDecimal precioUnitario,
        Boolean activo
) {

    public static PortafolioResponse from(Portafolio portafolio) {
        return new PortafolioResponse(
                portafolio.getId(),
                portafolio.getProveedorId(),
                portafolio.getServicioId(),
                portafolio.getPrecioUnitario(),
                portafolio.getActivo()
        );
    }
}
