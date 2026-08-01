package com.proarte.erp.catalogos.dto;

import java.util.UUID;

public record UnidadMedidaResponse(
        UUID id,
        String nombre,
        String abreviatura
) {}
