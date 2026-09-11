package com.proarte.erp.auth.service;

import com.proarte.erp.auth.dto.LoginRequest;
import com.proarte.erp.auth.dto.LoginResponse;
import com.proarte.erp.auth.dto.RefreshTokenRequest;
import com.proarte.erp.auth.dto.TokenResponse;
import com.proarte.erp.security.CustomUserDetails;
import com.proarte.erp.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

        @Mock
        private AuthenticationManager authenticationManager;

        @Mock
        private JwtTokenProvider jwtTokenProvider;

        @Mock
        private UserDetailsServiceImpl userDetailsService;

        @InjectMocks
        private AuthService authService;

        private CustomUserDetails createTestUserDetails() {
                UUID userId = UUID.randomUUID();
                UUID rolId = UUID.randomUUID();
                Map<String, Map<String, Boolean>> permisos = Map.of(
                                "usuarios", Map.of("leer", true, "crear", true));
                return new CustomUserDetails(userId, rolId, "admin", "encoded_pass", "Admin User", "Administrador",
                                permisos, true);
        }

        @Test
        @DisplayName("authenticate exitoso retorna LoginResponse con tokens y datos de usuario")
        void shouldAuthenticate_whenCredentialsAreValid() {
                LoginRequest request = new LoginRequest("admin", "password123");
                CustomUserDetails userDetails = createTestUserDetails();

                Authentication authentication = mock(Authentication.class);
                when(authentication.getPrincipal()).thenReturn(userDetails);
                when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                                .thenReturn(authentication);
                when(jwtTokenProvider.generateAccessToken(userDetails.getId(), userDetails.getRolId(), "admin", "Administrador"))
                                .thenReturn("access_token_123");
                when(jwtTokenProvider.generateRefreshToken(userDetails.getId(), "admin"))
                                .thenReturn("refresh_token_456");

                LoginResponse response = authService.authenticate(request);

                assertThat(response.accessToken()).isEqualTo("access_token_123");
                assertThat(response.refreshToken()).isEqualTo("refresh_token_456");
                assertThat(response.userId()).isEqualTo(userDetails.getId());
                assertThat(response.username()).isEqualTo("admin");
                assertThat(response.nombreCompleto()).isEqualTo("Admin User");
                assertThat(response.rol()).isEqualTo("Administrador");
                assertThat(response.permisos()).containsKey("usuarios");
        }

        @Test
        @DisplayName("authenticate lanza BadCredentialsException cuando credenciales son invalidas")
        void shouldThrowBadCredentials_whenAuthenticationFails() {
                LoginRequest request = new LoginRequest("admin", "wrong_password");
                when(authenticationManager.authenticate(any()))
                                .thenThrow(new BadCredentialsException("Bad credentials"));

                assertThatThrownBy(() -> authService.authenticate(request))
                                .isInstanceOf(BadCredentialsException.class)
                                .hasMessage("Credenciales inválidas");
        }

        @Test
        @DisplayName("refreshToken exitoso genera nuevos tokens cuando refresh token es valido")
        void shouldRefreshToken_whenRefreshTokenIsValid() {
                String validRefreshToken = "valid_refresh_token";
                RefreshTokenRequest request = new RefreshTokenRequest(validRefreshToken);
                CustomUserDetails userDetails = createTestUserDetails();

                when(jwtTokenProvider.validateToken(validRefreshToken)).thenReturn(true);
                when(jwtTokenProvider.getTokenType(validRefreshToken)).thenReturn("refresh");
                when(jwtTokenProvider.getUsername(validRefreshToken)).thenReturn("admin");
                when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
                when(jwtTokenProvider.generateAccessToken(userDetails.getId(), userDetails.getRolId(), "admin", "Administrador"))
                                .thenReturn("new_access_token");
                when(jwtTokenProvider.generateRefreshToken(userDetails.getId(), "admin"))
                                .thenReturn("new_refresh_token");

                TokenResponse response = authService.refreshToken(request);

                assertThat(response.accessToken()).isEqualTo("new_access_token");
                assertThat(response.refreshToken()).isEqualTo("new_refresh_token");
        }

        @Test
        @DisplayName("refreshToken lanza excepcion cuando token es invalido")
        void shouldThrowException_whenRefreshTokenIsInvalid() {
                RefreshTokenRequest request = new RefreshTokenRequest("invalid_token");
                when(jwtTokenProvider.validateToken("invalid_token")).thenReturn(false);

                assertThatThrownBy(() -> authService.refreshToken(request))
                                .isInstanceOf(BadCredentialsException.class)
                                .hasMessageContaining("inválido o expirado");
        }

        @Test
        @DisplayName("refreshToken lanza excepcion cuando token no es de tipo refresh")
        void shouldThrowException_whenTokenIsNotRefreshType() {
                String accessToken = "access_token_used_as_refresh";
                RefreshTokenRequest request = new RefreshTokenRequest(accessToken);

                when(jwtTokenProvider.validateToken(accessToken)).thenReturn(true);
                when(jwtTokenProvider.getTokenType(accessToken)).thenReturn("access");

                assertThatThrownBy(() -> authService.refreshToken(request))
                                .isInstanceOf(BadCredentialsException.class)
                                .hasMessageContaining("no es un refresh token");
        }
}
