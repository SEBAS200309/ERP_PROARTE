package com.proarte.erp.proveedores.dto;

import com.proarte.erp.proveedores.entity.Proveedor;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProveedorResponse(
        UUID id,
        UUID personaId,
        UUID empresaId,
        String especialidad,
        Boolean activo,
        UUID createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static ProveedorResponse from(Proveedor proveedor) {
        return new ProveedorResponse(
                proveedor.getId(),
                proveedor.getPersonaId(),
                proveedor.getEmpresaId(),
                proveedor.getEspecialidad(),
                proveedor.getActivo(),
                proveedor.getCreatedBy(),
                proveedor.getCreatedAt(),
                proveedor.getUpdatedAt()
        );
    }
}
