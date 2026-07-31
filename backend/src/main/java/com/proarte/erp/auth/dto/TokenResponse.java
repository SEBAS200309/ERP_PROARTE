package com.proarte.erp.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
