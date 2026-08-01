package com.proarte.erp.personas.dto;

public record UpdatePersonaRequest(
        String nombres,
        String apellidos,
        String tipoDocumento,
        String documento,
        String telefono,
        String email,
        String direccion,
        String rolPersona
) {
}
