package com.proarte.erp.presentaciones.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdatePresentacionRequest(
        @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
        String nombre,

        String descripcion,

        UUID servicioId
) {
}
