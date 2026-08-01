package com.proarte.erp.leads.dto;

import java.util.Map;

public record LeadEstadisticasResponse(
        Map<String, Long> estadisticas
) {
}
