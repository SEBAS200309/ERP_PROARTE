package com.proarte.erp.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateUsuarioRequest(
        @Size(max = 150, message = "El nombre completo no puede exceder 150 caracteres")
        String nombreCompleto,

        @Email(message = "El email debe tener un formato válido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        String email,

        UUID rolId,

        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String password
) {
}
