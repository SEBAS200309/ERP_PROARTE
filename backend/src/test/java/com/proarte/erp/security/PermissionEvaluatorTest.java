package com.proarte.erp.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionEvaluatorTest {

    private PermissionEvaluator permissionEvaluator;

    @BeforeEach
    void setUp() {
        permissionEvaluator = new PermissionEvaluator();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUser(Map<String, Map<String, Boolean>> permisos) {
        CustomUserDetails userDetails = new CustomUserDetails(
                UUID.randomUUID(), "admin", "pass", "Admin", "Administrador", permisos, true
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("hasPermission retorna true cuando usuario tiene permiso para modulo y accion")
    void shouldReturnTrue_whenUserHasPermission() {
        Map<String, Map<String, Boolean>> permisos = Map.of(
                "usuarios", Map.of("leer", true, "crear", true, "editar", false)
        );
        setAuthenticatedUser(permisos);

        assertThat(permissionEvaluator.hasPermission("usuarios", "leer")).isTrue();
        assertThat(permissionEvaluator.hasPermission("usuarios", "crear")).isTrue();
    }

    @Test
    @DisplayName("hasPermission retorna false cuando accion esta en false")
    void shouldReturnFalse_whenActionIsFalse() {
        Map<String, Map<String, Boolean>> permisos = Map.of(
                "usuarios", Map.of("leer", true, "editar", false)
        );
        setAuthenticatedUser(permisos);

        assertThat(permissionEvaluator.hasPermission("usuarios", "editar")).isFalse();
    }

    @Test
    @DisplayName("hasPermission retorna false cuando modulo no existe en permisos")
    void shouldReturnFalse_whenModuloDoesNotExist() {
        Map<String, Map<String, Boolean>> permisos = Map.of(
                "usuarios", Map.of("leer", true)
        );
        setAuthenticatedUser(permisos);

        assertThat(permissionEvaluator.hasPermission("eventos", "leer")).isFalse();
    }

    @Test
    @DisplayName("hasPermission retorna false cuando no hay autenticacion")
    void shouldReturnFalse_whenNotAuthenticated() {
        SecurityContextHolder.clearContext();

        assertThat(permissionEvaluator.hasPermission("usuarios", "leer")).isFalse();
    }

    @Test
    @DisplayName("hasPermission retorna false cuando permisos es null")
    void shouldReturnFalse_whenPermisosIsNull() {
        CustomUserDetails userDetails = new CustomUserDetails(
                UUID.randomUUID(), "admin", "pass", "Admin", "Administrador", null, true
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(permissionEvaluator.hasPermission("usuarios", "leer")).isFalse();
    }

    @Test
    @DisplayName("hasPermission retorna false cuando principal no es CustomUserDetails")
    void shouldReturnFalse_whenPrincipalIsNotCustomUserDetails() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("simpleUser", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(permissionEvaluator.hasPermission("usuarios", "leer")).isFalse();
    }
}
