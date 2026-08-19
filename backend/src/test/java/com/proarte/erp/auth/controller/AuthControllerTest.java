package com.proarte.erp.auth.controller;

import com.proarte.erp.auth.dto.LoginRequest;
import com.proarte.erp.auth.dto.LoginResponse;
import com.proarte.erp.auth.dto.RefreshTokenRequest;
import com.proarte.erp.auth.dto.TokenResponse;
import com.proarte.erp.auth.service.AuthService;
import com.proarte.erp.controller.BaseControllerTest;
import com.proarte.erp.controller.TestSecurityConfig;
import com.proarte.erp.exception.GlobalExceptionHandler;
import com.proarte.erp.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import({ TestSecurityConfig.class, GlobalExceptionHandler.class })
class AuthControllerTest extends BaseControllerTest {

        @MockBean
        private AuthService authService;

        // ==================== Login Tests ====================

        @Test
        void login_withValidCredentials_returns200() throws Exception {
                LoginResponse response = new LoginResponse(
                                "access-token", "refresh-token",
                                TEST_USER_ID, TEST_ROL_ID, TEST_USERNAME, "Admin User", "Administrador",
                                Map.of("leads", Map.of("leer", true)));
                when(authService.authenticate(any(LoginRequest.class))).thenReturn(response);

                LoginRequest request = new LoginRequest("admin", "password123");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("access-token"))
                                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                                .andExpect(jsonPath("$.username").value(TEST_USERNAME));
        }

        @Test
        void login_withInvalidCredentials_returns401() throws Exception {
                when(authService.authenticate(any(LoginRequest.class)))
                                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

                LoginRequest request = new LoginRequest("admin", "wrongpassword");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("ERR_AUTH"));
        }

        @Test
        void login_withBlankUsername_returns400() throws Exception {
                LoginRequest request = new LoginRequest("", "password123");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION"));
        }

        @Test
        void login_withBlankPassword_returns400() throws Exception {
                LoginRequest request = new LoginRequest("admin", "");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION"));
        }

        // ==================== Refresh Token Tests ====================

        @Test
        void refreshToken_withValidToken_returns200() throws Exception {
                TokenResponse response = new TokenResponse("new-access-token", "new-refresh-token");
                when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

                RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

                mockMvc.perform(post("/api/v1/auth/refresh-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
        }

        @Test
        void refreshToken_withInvalidToken_returns401() throws Exception {
                when(authService.refreshToken(any(RefreshTokenRequest.class)))
                                .thenThrow(new BadCredentialsException("Refresh token inválido"));

                RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");

                mockMvc.perform(post("/api/v1/auth/refresh-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("ERR_AUTH"));
        }

        @Test
        void refreshToken_withBlankToken_returns400() throws Exception {
                RefreshTokenRequest request = new RefreshTokenRequest("");

                mockMvc.perform(post("/api/v1/auth/refresh-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION"));
        }

        // ==================== Logout Tests ====================

        @Test
        void logout_withAuthentication_returns200() throws Exception {
                mockMvc.perform(post("/api/v1/auth/logout")
                                .with(withPermission("usuarios")))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.mensaje").value("Sesión cerrada exitosamente"));
        }

        @Test
        void logout_withoutAuthentication_returns401() throws Exception {
                mockMvc.perform(post("/api/v1/auth/logout"))
                                .andExpect(status().isUnauthorized());
        }
}
