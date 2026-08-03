package com.proarte.erp.security;

import com.proarte.erp.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private static final String TEST_SECRET = "EstaEsUnaClaveDePruebaConAlMenos32CaracteresParaHMAC256";
    private static final long ACCESS_EXPIRATION = 3600000L; // 1 hour
    private static final long REFRESH_EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(TEST_SECRET, ACCESS_EXPIRATION, REFRESH_EXPIRATION);
        jwtTokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    @DisplayName("generateAccessToken genera un token valido con claims correctos")
    void shouldGenerateAccessToken_withCorrectClaims() {
        UUID userId = UUID.randomUUID();
        String username = "admin";
        String rol = "Administrador";

        String token = jwtTokenProvider.generateAccessToken(userId, username, rol);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsername(token)).isEqualTo(username);
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getRol(token)).isEqualTo(rol);
        assertThat(jwtTokenProvider.getTokenType(token)).isEqualTo("access");
    }

    @Test
    @DisplayName("generateRefreshToken genera un token valido con type=refresh")
    void shouldGenerateRefreshToken_withCorrectClaims() {
        UUID userId = UUID.randomUUID();
        String username = "usuario1";

        String token = jwtTokenProvider.generateRefreshToken(userId, username);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsername(token)).isEqualTo(username);
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId);
        assertThat(jwtTokenProvider.getTokenType(token)).isEqualTo("refresh");
    }

    @Test
    @DisplayName("validateToken retorna false para token invalido")
    void shouldReturnFalse_whenTokenIsInvalid() {
        assertThat(jwtTokenProvider.validateToken("token.invalido.aqui")).isFalse();
    }

    @Test
    @DisplayName("validateToken retorna false para token null")
    void shouldReturnFalse_whenTokenIsNull() {
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("validateToken retorna false para token vacio")
    void shouldReturnFalse_whenTokenIsEmpty() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("validateToken retorna false para token expirado")
    void shouldReturnFalse_whenTokenIsExpired() {
        JwtProperties expiredProps = new JwtProperties(TEST_SECRET, -1000L, -1000L);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);

        String token = expiredProvider.generateAccessToken(UUID.randomUUID(), "user", "rol");

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("validateToken retorna false para token firmado con otra clave")
    void shouldReturnFalse_whenTokenSignedWithDifferentKey() {
        String otherSecret = "OtraClaveDePruebaQueTiene32CaracteresComoMinimo!!";
        JwtProperties otherProps = new JwtProperties(otherSecret, ACCESS_EXPIRATION, REFRESH_EXPIRATION);
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);

        String token = otherProvider.generateAccessToken(UUID.randomUUID(), "user", "rol");

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("getRol retorna null para refresh token (no tiene claim rol)")
    void shouldReturnNull_whenGettingRolFromRefreshToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateRefreshToken(userId, "user");

        assertThat(jwtTokenProvider.getRol(token)).isNull();
    }

    @Test
    @DisplayName("access y refresh tokens tienen tipos diferentes")
    void shouldGenerateDifferentTokenTypes() {
        UUID userId = UUID.randomUUID();
        String accessToken = jwtTokenProvider.generateAccessToken(userId, "user", "rol");
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId, "user");

        assertThat(jwtTokenProvider.getTokenType(accessToken)).isEqualTo("access");
        assertThat(jwtTokenProvider.getTokenType(refreshToken)).isEqualTo("refresh");
    }
}
