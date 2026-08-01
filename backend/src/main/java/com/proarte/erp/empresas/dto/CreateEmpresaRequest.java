package com.proarte.erp.empresas.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateEmpresaRequest(
        @NotBlank(message = "La razón social es obligatoria")
        String razonSocial,

        String nit,

        String direccion,

        String telefono,

        String email,

        String rolEmpresa
) {
}
