package com.proarte.erp.leads.dto;

import com.proarte.erp.leads.entity.Lead;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LeadResponse(
        UUID id,
        String descripcion,
        UUID estadoId,
        UUID personaId,
        UUID empresaId,
        UUID createdBy,
        Boolean activo,
        OffsetDateTime createdAt
) {

    public static LeadResponse from(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getDescripcion(),
                lead.getEstadoId(),
                lead.getPersonaId(),
                lead.getEmpresaId(),
                lead.getCreatedBy(),
                lead.getActivo(),
                lead.getCreatedAt()
        );
    }
}
