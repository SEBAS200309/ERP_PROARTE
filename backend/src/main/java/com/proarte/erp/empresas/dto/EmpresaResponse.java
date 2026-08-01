package com.proarte.erp.empresas.dto;

import com.proarte.erp.empresas.entity.Empresa;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EmpresaResponse(
        UUID id,
        String razonSocial,
        String nit,
        String direccion,
        String telefono,
        String email,
        UUID rolEntidadId,
        Boolean activo,
        UUID createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static EmpresaResponse from(Empresa empresa) {
        return new EmpresaResponse(
                empresa.getId(),
                empresa.getRazonSocial(),
                empresa.getNit(),
                empresa.getDireccion(),
                empresa.getTelefono(),
                empresa.getEmail(),
                empresa.getRolEntidadId(),
                empresa.getActivo(),
                empresa.getCreatedBy(),
                empresa.getCreatedAt(),
                empresa.getUpdatedAt()
        );
    }
}
