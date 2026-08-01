package com.proarte.erp.mensajes.dto;

import jakarta.validation.constraints.Size;

public record UpdateMensajeRequest(
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        String contenido
) {
}
