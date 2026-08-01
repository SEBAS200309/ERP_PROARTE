package com.proarte.erp.servicios.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateDescuentoRecargoRequest(
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @DecimalMin(value = "0.00", message = "El valor debe ser mayor o igual a 0")
        @DecimalMax(value = "100.00", message = "El valor debe ser menor o igual a 100")
        BigDecimal valor,

        UUID tipoId
) {
}
