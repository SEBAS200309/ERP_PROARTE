package com.proarte.erp.leads.dto;

import java.util.UUID;

public record UpdateLeadRequest(
        String descripcion,
        String estado,
        UUID personaId,
        UUID empresaId
) {
}
