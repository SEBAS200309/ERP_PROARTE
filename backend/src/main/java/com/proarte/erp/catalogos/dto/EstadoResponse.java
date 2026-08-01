package com.proarte.erp.catalogos.dto;

import java.util.UUID;

public record EstadoResponse(
        UUID id,
        String nombre,
        String contexto
) {}
