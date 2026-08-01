package com.proarte.erp.personas.dto;

import com.proarte.erp.personas.entity.Persona;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PersonaResponse(
        UUID id,
        String nombres,
        String apellidos,
        String tipoDocumento,
        String documento,
        String telefono,
        String email,
        String direccion,
        String rolPersona,
        Boolean activo,
        UUID createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static PersonaResponse from(Persona persona) {
        return new PersonaResponse(
                persona.getId(),
                persona.getNombres(),
                persona.getApellidos(),
                persona.getTipoDocumento(),
                persona.getDocumento(),
                persona.getTelefono(),
                persona.getEmail(),
                persona.getDireccion(),
                persona.getRolPersona(),
                persona.getActivo(),
                persona.getCreatedBy(),
                persona.getCreatedAt(),
                persona.getUpdatedAt()
        );
    }
}
