package com.proarte.erp.personas.dto;

import java.util.UUID;

public record UpdatePersonaRequest(
        String nombres,
        String apellidos,
        UUID tipoDocumentoId,
        String documento,
        String telefono,
        String email,
        String direccion,
        UUID rolEntidadId
) {
}
