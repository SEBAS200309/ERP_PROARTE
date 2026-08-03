package com.proarte.erp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.proarte.erp.security.CustomUserDetails;
import com.proarte.erp.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Map;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/**
 * Base class for all controller tests providing common configuration and helpers.
 * 
 * All subclasses must use:
 * - @WebMvcTest(ControllerClass.class)
 * - @Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
 * 
 * TestSecurityConfig replaces the production SecurityConfig and JwtAuthenticationFilter,
 * providing a simple security chain controlled via test RequestPostProcessors.
 */
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockBean
    protected UserDetailsService userDetailsService;

    protected static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    protected static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    protected static final String TEST_USERNAME = "admin";
    protected static final String TEST_PASSWORD = "password123";

    /**
     * Creates a CustomUserDetails with full permissions for the given module.
     */
    protected CustomUserDetails createUserWithPermission(String modulo) {
        Map<String, Map<String, Boolean>> permisos = Map.of(
                modulo, Map.of("leer", true, "crear", true, "editar", true, "eliminar", true)
        );
        return new CustomUserDetails(
                TEST_USER_ID, TEST_USERNAME, TEST_PASSWORD,
                "Admin User", "Administrador", permisos, true
        );
    }

    /**
     * Creates a CustomUserDetails with NO permissions at all.
     */
    protected CustomUserDetails createUserWithoutPermission() {
        return new CustomUserDetails(
                TEST_USER_ID, TEST_USERNAME, TEST_PASSWORD,
                "Admin User", "Administrador", Map.of(), true
        );
    }

    /**
     * Returns a RequestPostProcessor that populates the SecurityContext with 
     * a UsernamePasswordAuthenticationToken holding the given CustomUserDetails.
     */
    protected RequestPostProcessor authenticatedUser(CustomUserDetails userDetails) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        return authentication(auth);
    }

    /**
     * Returns a RequestPostProcessor for a user with full permissions on the given module.
     */
    protected RequestPostProcessor withPermission(String modulo) {
        return authenticatedUser(createUserWithPermission(modulo));
    }

    /**
     * Returns a RequestPostProcessor for a user with no permissions.
     */
    protected RequestPostProcessor withNoPermission() {
        return authenticatedUser(createUserWithoutPermission());
    }
}
