package com.proarte.erp.auth.service;

import com.proarte.erp.auth.dto.LoginRequest;
import com.proarte.erp.auth.dto.LoginResponse;
import com.proarte.erp.auth.dto.RefreshTokenRequest;
import com.proarte.erp.auth.dto.TokenResponse;
import com.proarte.erp.security.CustomUserDetails;
import com.proarte.erp.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    public LoginResponse authenticate(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            String accessToken = jwtTokenProvider.generateAccessToken(
                    userDetails.getId(), userDetails.getUsername(), userDetails.getRolNombre());
            String refreshToken = jwtTokenProvider.generateRefreshToken(
                    userDetails.getId(), userDetails.getUsername());

            log.info("Usuario autenticado exitosamente: {}", userDetails.getUsername());

            return new LoginResponse(
                    accessToken,
                    refreshToken,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getNombreCompleto(),
                    userDetails.getRolNombre(),
                    userDetails.getPermisos()
            );
        } catch (BadCredentialsException e) {
            log.warn("Intento de login fallido para usuario: {}", request.username());
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Refresh token inválido o expirado");
        }

        String tokenType = jwtTokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new BadCredentialsException("El token proporcionado no es un refresh token");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);

        // Reload user to verify they're still active
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                userDetails.getId(), userDetails.getUsername(), userDetails.getRolNombre());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                userDetails.getId(), userDetails.getUsername());

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
