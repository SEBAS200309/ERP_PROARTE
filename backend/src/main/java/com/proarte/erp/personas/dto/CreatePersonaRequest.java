package com.proarte.erp.personas.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreatePersonaRequest(
        @NotBlank(message = "Los nombres son obligatorios")
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios")
        String apellidos,

        UUID tipoDocumentoId,

        String documento,

        String telefono,

        String email,

        String direccion,

        UUID rolEntidadId
) {
}
