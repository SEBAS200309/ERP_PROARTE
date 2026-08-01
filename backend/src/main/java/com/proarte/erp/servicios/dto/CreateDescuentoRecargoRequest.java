package com.proarte.erp.servicios.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateDescuentoRecargoRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @NotNull(message = "El valor es obligatorio")
        @DecimalMin(value = "0.00", message = "El valor debe ser mayor o igual a 0")
        @DecimalMax(value = "100.00", message = "El valor debe ser menor o igual a 100")
        BigDecimal valor,

        @NotNull(message = "El tipoId es obligatorio")
        UUID tipoId
) {
}
