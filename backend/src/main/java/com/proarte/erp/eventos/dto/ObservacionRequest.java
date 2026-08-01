package com.proarte.erp.eventos.dto;

import jakarta.validation.constraints.NotBlank;

public record ObservacionRequest(
        @NotBlank(message = "El texto de la observación es obligatorio")
        String texto
) {
}
