package com.proarte.erp.empresas.dto;

public record UpdateEmpresaRequest(
        String razonSocial,
        String nit,
        String direccion,
        String telefono,
        String email,
        String rolEmpresa
) {
}
