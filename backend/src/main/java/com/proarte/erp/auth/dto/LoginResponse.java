package com.proarte.erp.auth.dto;

import java.util.Map;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        UUID rolId,
        String username,
        String nombreCompleto,
        String rol,
        Map<String, Map<String, Boolean>> permisos
) {
}
