package com.proarte.erp.catalogos.controller;

import com.proarte.erp.catalogos.dto.CreateCatalogoRequest;
import com.proarte.erp.catalogos.service.CatalogoService;
import com.proarte.erp.controller.BaseControllerTest;
import com.proarte.erp.controller.TestSecurityConfig;
import com.proarte.erp.exception.GlobalExceptionHandler;
import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.security.PermissionEvaluator;
import com.proarte.erp.security.SecurityConfig;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CatalogoController.class, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class CatalogoControllerTest extends BaseControllerTest {

    @MockBean
    private CatalogoService catalogoService;

    @MockBean
    private PermissionEvaluator permissionEvaluator;

    private static final UUID CATALOGO_ID = UUID.randomUUID();

    // ==================== Authentication Tests ====================

    @Test
    void getAll_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/catalogos/estados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_withoutAuth_returns401() throws Exception {
        CreateCatalogoRequest request = new CreateCatalogoRequest("Nuevo Estado", null, "NE");

        mockMvc.perform(post("/api/v1/catalogos/estados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Authorization Tests ====================

    @Test
    void getAll_withoutPermission_returnsUnauthorized() throws Exception {
        when(permissionEvaluator.hasPermission("catalogos", "leer")).thenReturn(false);

        mockMvc.perform(get("/api/v1/catalogos/estados")
                        .with(withNoPermission()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_AUTH"));
    }

    // ==================== Success Tests ====================

    @Test
    void getAll_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("catalogos", "leer")).thenReturn(true);

        List<?> items = List.of(
                Map.of("id", CATALOGO_ID.toString(), "nombre", "Nuevo"),
                Map.of("id", UUID.randomUUID().toString(), "nombre", "En Proceso")
        );
        doReturn(items).when(catalogoService).getAll(eq("estados"), any());

        mockMvc.perform(get("/api/v1/catalogos/estados")
                        .with(withPermission("catalogos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].nombre").value("Nuevo"));
    }

    @Test
    void getById_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("catalogos", "leer")).thenReturn(true);

        Map<String, Object> item = Map.of("id", CATALOGO_ID.toString(), "nombre", "Activo");
        when(catalogoService.getById("estados", CATALOGO_ID)).thenReturn(item);

        mockMvc.perform(get("/api/v1/catalogos/estados/" + CATALOGO_ID)
                        .with(withPermission("catalogos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nombre").value("Activo"));
    }

    @Test
    void create_withValidData_returns201() throws Exception {
        when(permissionEvaluator.hasPermission("catalogos", "crear")).thenReturn(true);

        Map<String, Object> created = Map.of("id", CATALOGO_ID.toString(), "nombre", "Nuevo Estado");
        when(catalogoService.create(eq("estados"), any(CreateCatalogoRequest.class))).thenReturn(created);

        CreateCatalogoRequest request = new CreateCatalogoRequest("Nuevo Estado", null, "NE");

        mockMvc.perform(post("/api/v1/catalogos/estados")
                        .with(withPermission("catalogos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registro de catálogo creado exitosamente"));
    }

    @Test
    void update_withValidData_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("catalogos", "editar")).thenReturn(true);

        Map<String, Object> updated = Map.of("id", CATALOGO_ID.toString(), "nombre", "Actualizado");
        when(catalogoService.update(eq("estados"), eq(CATALOGO_ID), any(CreateCatalogoRequest.class))).thenReturn(updated);

        CreateCatalogoRequest request = new CreateCatalogoRequest("Actualizado", null, "AC");

        mockMvc.perform(put("/api/v1/catalogos/estados/" + CATALOGO_ID)
                        .with(withPermission("catalogos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registro de catálogo actualizado exitosamente"));
    }

    @Test
    void delete_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("catalogos", "eliminar")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/catalogos/estados/" + CATALOGO_ID)
                        .with(withPermission("catalogos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registro de catálogo eliminado exitosamente"));
    }

    // ==================== Validation Tests ====================

    @Test
    void create_withBlankNombre_returns400() throws Exception {
        when(permissionEvaluator.hasPermission("catalogos", "crear")).thenReturn(true);

        CreateCatalogoRequest request = new CreateCatalogoRequest("", null, "");

        mockMvc.perform(post("/api/v1/catalogos/estados")
                        .with(withPermission("catalogos"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION"));
    }

    // ==================== Not Found Tests ====================

    @Test
    void getById_notFound_returns404() throws Exception {
        when(permissionEvaluator.hasPermission("catalogos", "leer")).thenReturn(true);

        UUID nonExistentId = UUID.randomUUID();
        when(catalogoService.getById("estados", nonExistentId))
                .thenThrow(new ResourceNotFoundException("Registro de catálogo no encontrado"));

        mockMvc.perform(get("/api/v1/catalogos/estados/" + nonExistentId)
                        .with(withPermission("catalogos")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_NOT_FOUND"));
    }

    // ==================== Different Types ====================

    @Test
    void getAll_withDifferentTipo_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("catalogos", "leer")).thenReturn(true);

        List<?> items = List.of(Map.of("id", UUID.randomUUID().toString(), "nombre", "Catering"));
        doReturn(items).when(catalogoService).getAll(eq("servicios"), any());

        mockMvc.perform(get("/api/v1/catalogos/servicios")
                        .with(withPermission("catalogos")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }
}
