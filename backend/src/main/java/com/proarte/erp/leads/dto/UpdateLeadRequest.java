package com.proarte.erp.leads.dto;

import java.util.UUID;

public record UpdateLeadRequest(
        String descripcion,
        UUID estadoId,
        UUID personaId,
        UUID empresaId
) {
}
