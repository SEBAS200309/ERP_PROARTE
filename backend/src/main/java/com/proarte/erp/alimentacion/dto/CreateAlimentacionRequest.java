package com.proarte.erp.alimentacion.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAlimentacionRequest(
        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a cero")
        BigDecimal cantidad,

        @Size(max = 200, message = "La descripción no puede exceder 200 caracteres")
        String descripcion
) {}
