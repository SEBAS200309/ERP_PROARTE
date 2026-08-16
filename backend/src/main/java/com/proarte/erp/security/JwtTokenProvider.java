package com.proarte.erp.security;

import com.proarte.erp.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final JwtProperties jwtProperties;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }
    //Genracion del token de inicio de sesion (propio de springboot)
    public String generateAccessToken(UUID userId, UUID rol_id, String username, String rol) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.accessTokenExpiration());

        return Jwts.builder()
                .subject(username)
                .claims(Map.of(
                        "userId", userId.toString(),
                        "rolId", rolId.toString,
                        "rol", rol,
                        "type", "access"
                ))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
    // Refrescar el token para que no se cierre la sesion
    public String generateRefreshToken(UUID userId, String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.refreshTokenExpiration());

        return Jwts.builder()
                .subject(username)
                .claims(Map.of(
                        "userId", userId.toString(),
                        "type", "refresh"
                ))
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }
    
    // Validacion del token (de la firma y expiracion del JWT)
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }
    
//Getters de los datos dentro del token

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public UUID getUserId(String token) {
        return UUID.fromString(getClaims(token).get("userId", String.class));
    }

    public UUID getRolId(String token) {
        return UUID.fromString(
                getClaims(token).get("rolId", String.class)
        );
    }

    public String getRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    public String getTokenType(String token) {
        return getClaims(token).get("type", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}