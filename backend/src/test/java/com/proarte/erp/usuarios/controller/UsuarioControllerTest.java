package com.proarte.erp.usuarios.controller;

import com.proarte.erp.auth.entity.Usuario;
import com.proarte.erp.controller.BaseControllerTest;
import com.proarte.erp.controller.TestSecurityConfig;
import com.proarte.erp.exception.GlobalExceptionHandler;
import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.security.PermissionEvaluator;
import com.proarte.erp.security.SecurityConfig;
import com.proarte.erp.usuarios.dto.CreateUsuarioRequest;
import com.proarte.erp.usuarios.dto.UpdateUsuarioRequest;
import com.proarte.erp.usuarios.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UsuarioController.class, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class UsuarioControllerTest extends BaseControllerTest {

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private PermissionEvaluator permissionEvaluator;

    private static final UUID USUARIO_ID = UUID.randomUUID();
    private static final UUID ROL_ID = UUID.randomUUID();

    // ==================== Authentication Tests ====================

    @Test
    void getAll_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_withoutAuth_returns401() throws Exception {
        CreateUsuarioRequest request = new CreateUsuarioRequest(
                "newuser", "password123", "New User", "new@test.com", ROL_ID);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Authorization Tests ====================

    @Test
    void getAll_withoutPermission_returnsUnauthorized() throws Exception {
        when(permissionEvaluator.hasPermission("usuarios", "leer")).thenReturn(false);

        mockMvc.perform(get("/api/v1/usuarios")
                        .with(withNoPermission()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_AUTH"));
    }

    // ==================== Success Tests ====================

    @Test
    void getAll_withPermission_returns200WithPagination() throws Exception {
        when(permissionEvaluator.hasPermission("usuarios", "leer")).thenReturn(true);

        Usuario usuario = createTestUsuario();
        Page<Usuario> page = new PageImpl<>(List.of(usuario));
        when(usuarioService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/usuarios")
                        .with(withPermission("usuarios"))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(USUARIO_ID.toString()))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.page").value(0));
    }

    @Test
    void getById_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("usuarios", "leer")).thenReturn(true);

        Usuario usuario = createTestUsuario();
        when(usuarioService.getById(USUARIO_ID)).thenReturn(usuario);

        mockMvc.perform(get("/api/v1/usuarios/" + USUARIO_ID)
                        .with(withPermission("usuarios")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(USUARIO_ID.toString()))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    void create_withValidData_returns201() throws Exception {
        when(permissionEvaluator.hasPermission("usuarios", "crear")).thenReturn(true);

        Usuario usuario = createTestUsuario();
        when(usuarioService.create(any(CreateUsuarioRequest.class))).thenReturn(usuario);

        CreateUsuarioRequest request = new CreateUsuarioRequest(
                "testuser", "password123", "Test User", "test@test.com", ROL_ID);

        mockMvc.perform(post("/api/v1/usuarios")
                        .with(withPermission("usuarios"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(USUARIO_ID.toString()))
                .andExpect(jsonPath("$.message").value("Usuario creado exitosamente"));
    }

    @Test
    void update_withValidData_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("usuarios", "editar")).thenReturn(true);

        Usuario usuario = createTestUsuario();
        when(usuarioService.update(eq(USUARIO_ID), any(UpdateUsuarioRequest.class))).thenReturn(usuario);

        UpdateUsuarioRequest request = new UpdateUsuarioRequest("Updated User", "updated@test.com", ROL_ID, null);

        mockMvc.perform(put("/api/v1/usuarios/" + USUARIO_ID)
                        .with(withPermission("usuarios"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario actualizado exitosamente"));
    }

    @Test
    void delete_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("usuarios", "eliminar")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/usuarios/" + USUARIO_ID)
                        .with(withPermission("usuarios")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuario eliminado exitosamente"));
    }

    // ==================== Validation Tests ====================

    @Test
    void create_withBlankUsername_returns400() throws Exception {
        when(permissionEvaluator.hasPermission("usuarios", "crear")).thenReturn(true);

        CreateUsuarioRequest request = new CreateUsuarioRequest(
                "", "password123", "Test User", "test@test.com", ROL_ID);

        mockMvc.perform(post("/api/v1/usuarios")
                        .with(withPermission("usuarios"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION"));
    }

    @Test
    void create_withShortPassword_returns400() throws Exception {
        when(permissionEvaluator.hasPermission("usuarios", "crear")).thenReturn(true);

        CreateUsuarioRequest request = new CreateUsuarioRequest(
                "testuser", "12345", "Test User", "test@test.com", ROL_ID);

        mockMvc.perform(post("/api/v1/usuarios")
                        .with(withPermission("usuarios"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION"));
    }

    // ==================== Not Found Tests ====================

    @Test
    void getById_notFound_returns404() throws Exception {
        when(permissionEvaluator.hasPermission("usuarios", "leer")).thenReturn(true);

        UUID nonExistentId = UUID.randomUUID();
        when(usuarioService.getById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Usuario no encontrado"));

        mockMvc.perform(get("/api/v1/usuarios/" + nonExistentId)
                        .with(withPermission("usuarios")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_NOT_FOUND"));
    }

    // ==================== Helpers ====================

    private Usuario createTestUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(USUARIO_ID);
        usuario.setUsername("testuser");
        usuario.setNombreCompleto("Test User");
        usuario.setEmail("test@test.com");
        usuario.setRolId(ROL_ID);
        usuario.setActivo(true);
        usuario.setCreatedAt(OffsetDateTime.now());
        return usuario;
    }
}
