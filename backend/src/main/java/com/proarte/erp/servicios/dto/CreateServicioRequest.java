package com.proarte.erp.servicios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateServicioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
        String nombre,

        String descripcion,

        Boolean esPropio,

        Boolean requiereOc,

        UUID servicioPadreId,

        UUID categoriaId
) {
}
