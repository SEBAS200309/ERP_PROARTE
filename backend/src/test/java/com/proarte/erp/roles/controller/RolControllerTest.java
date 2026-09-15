package com.proarte.erp.roles.controller;

import com.proarte.erp.controller.BaseControllerTest;
import com.proarte.erp.controller.TestSecurityConfig;
import com.proarte.erp.exception.GlobalExceptionHandler;
import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.roles.dto.CreateRolRequest;
import com.proarte.erp.roles.dto.RolResponse;
import com.proarte.erp.roles.dto.UpdateRolRequest;
import com.proarte.erp.roles.service.RolService;
import com.proarte.erp.security.PermissionEvaluator;
import com.proarte.erp.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = RolController.class, excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class RolControllerTest extends BaseControllerTest {

    @MockBean
    private RolService rolService;

    @MockBean
    private PermissionEvaluator permissionEvaluator;

    private static final UUID ROL_ID = UUID.randomUUID();

    @Test
    @DisplayName("getAll sin autenticación retorna 401")
    void getAll_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("getAll sin permiso retorna ERR_AUTH")
    void getAll_withoutPermission_returnsUnauthorized() throws Exception {
        when(permissionEvaluator.hasPermission("roles", "leer")).thenReturn(false);

        mockMvc.perform(get("/api/v1/roles")
                        .with(withNoPermission()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_AUTH"));
    }

    @Test
    @DisplayName("getAll con permiso retorna lista de roles en ApiResponse")
    void getAll_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("roles", "leer")).thenReturn(true);

        RolResponse rol = new RolResponse(ROL_ID, "Administrador", "Desc", Map.of("roles", Map.of("leer", true)));
        when(rolService.getAll()).thenReturn(List.of(rol));

        mockMvc.perform(get("/api/v1/roles")
                        .with(withPermission("roles")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(ROL_ID.toString()))
                .andExpect(jsonPath("$.data[0].nombre").value("Administrador"))
                .andExpect(jsonPath("$.data[0].configuracion.roles.leer").value(true));
    }

    @Test
    @DisplayName("getById con permiso retorna rol en ApiResponse")
    void getById_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("roles", "leer")).thenReturn(true);

        RolResponse rol = new RolResponse(ROL_ID, "Comercial", "Desc", Map.of());
        when(rolService.getById(ROL_ID)).thenReturn(rol);

        mockMvc.perform(get("/api/v1/roles/" + ROL_ID)
                        .with(withPermission("roles")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(ROL_ID.toString()))
                .andExpect(jsonPath("$.data.nombre").value("Comercial"));
    }

    @Test
    @DisplayName("getById retorna 404 cuando rol no existe")
    void getById_notFound_returns404() throws Exception {
        when(permissionEvaluator.hasPermission("roles", "leer")).thenReturn(true);
        when(rolService.getById(ROL_ID)).thenThrow(new ResourceNotFoundException("Rol", "id", ROL_ID));

        mockMvc.perform(get("/api/v1/roles/" + ROL_ID)
                        .with(withPermission("roles")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_NOT_FOUND"));
    }

    @Test
    @DisplayName("create con datos válidos retorna 201 Created")
    void create_withValidData_returns201() throws Exception {
        when(permissionEvaluator.hasPermission("roles", "crear")).thenReturn(true);

        CreateRolRequest request = new CreateRolRequest("Operativo", "Rol operativo", Map.of("eventos", Map.of("crear", true)));
        RolResponse response = new RolResponse(ROL_ID, "Operativo", "Rol operativo", request.configuracion());
        when(rolService.create(any(CreateRolRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/roles")
                        .with(withPermission("roles"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(ROL_ID.toString()))
                .andExpect(jsonPath("$.message").value("Rol creado exitosamente"));
    }

    @Test
    @DisplayName("create con nombre en blanco retorna 400 Validation Error")
    void create_withBlankName_returns400() throws Exception {
        when(permissionEvaluator.hasPermission("roles", "crear")).thenReturn(true);

        CreateRolRequest request = new CreateRolRequest("", "Desc", Map.of());

        mockMvc.perform(post("/api/v1/roles")
                        .with(withPermission("roles"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION"));
    }

    @Test
    @DisplayName("create con configuracion nula retorna 400 Validation Error")
    void create_withNullConfiguracion_returns400() throws Exception {
        when(permissionEvaluator.hasPermission("roles", "crear")).thenReturn(true);

        String jsonWithoutConfig = "{\"nombre\":\"Test\",\"descripcion\":\"Desc\"}";

        mockMvc.perform(post("/api/v1/roles")
                        .with(withPermission("roles"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithoutConfig))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION"));
    }

    @Test
    @DisplayName("update con datos válidos retorna 200 OK")
    void update_withValidData_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("roles", "editar")).thenReturn(true);

        UpdateRolRequest request = new UpdateRolRequest("Admin Modificado", "Nueva desc", Map.of());
        RolResponse response = new RolResponse(ROL_ID, "Admin Modificado", "Nueva desc", Map.of());
        when(rolService.update(eq(ROL_ID), any(UpdateRolRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/roles/" + ROL_ID)
                        .with(withPermission("roles"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nombre").value("Admin Modificado"))
                .andExpect(jsonPath("$.message").value("Rol actualizado exitosamente"));
    }

    @Test
    @DisplayName("delete con permiso retorna 200 OK")
    void delete_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("roles", "eliminar")).thenReturn(true);
        doNothing().when(rolService).delete(ROL_ID);

        mockMvc.perform(delete("/api/v1/roles/" + ROL_ID)
                        .with(withPermission("roles")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Rol eliminado exitosamente"));
    }
}
