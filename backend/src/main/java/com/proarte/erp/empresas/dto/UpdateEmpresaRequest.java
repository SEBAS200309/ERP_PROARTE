package com.proarte.erp.empresas.dto;

import java.util.UUID;

public record UpdateEmpresaRequest(
        String razonSocial,
        String nit,
        String direccion,
        String telefono,
        String email,
        UUID rolEntidadId
) {
}
