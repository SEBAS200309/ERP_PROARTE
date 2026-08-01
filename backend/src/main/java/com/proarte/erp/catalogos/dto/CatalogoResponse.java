package com.proarte.erp.catalogos.dto;

import java.util.UUID;

public record CatalogoResponse(
        UUID id,
        String nombre
) {}
