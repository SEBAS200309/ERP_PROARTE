package com.proarte.erp.personas.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePersonaRequest(
        @NotBlank(message = "Los nombres son obligatorios")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        String apellidos,

        String tipoDocumento,

        String documento,

        String telefono,

        String email,

        String direccion,

        String rolPersona
) {
}
