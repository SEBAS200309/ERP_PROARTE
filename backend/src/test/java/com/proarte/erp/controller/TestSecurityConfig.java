package com.proarte.erp.controller;

import com.proarte.erp.config.CorsProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Test security configuration for controller slice tests.
 * Replaces the production SecurityConfig (which requires JwtAuthenticationFilter).
 * Uses a simple filter chain without JWT, allowing tests to control authentication
 * via spring-security-test's SecurityMockMvcRequestPostProcessors.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh-token"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"success\":false,\"error\":{\"code\":\"ERR_AUTH\",\"message\":\"No autenticado\"},\"message\":null}");
                        })
                )
                .build();
    }

    @Bean
    public CorsProperties corsProperties() {
        return new CorsProperties(List.of("http://localhost:4200"));
    }
}
