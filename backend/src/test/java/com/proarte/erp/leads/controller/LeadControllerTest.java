package com.proarte.erp.leads.controller;

import com.proarte.erp.controller.BaseControllerTest;
import com.proarte.erp.controller.TestSecurityConfig;
import com.proarte.erp.exception.GlobalExceptionHandler;
import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.leads.dto.CreateLeadRequest;
import com.proarte.erp.leads.dto.UpdateLeadRequest;
import com.proarte.erp.leads.entity.Lead;
import com.proarte.erp.leads.service.LeadService;
import com.proarte.erp.security.PermissionEvaluator;
import com.proarte.erp.security.SecurityConfig;
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
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = LeadController.class, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class LeadControllerTest extends BaseControllerTest {

    @MockBean
    private LeadService leadService;

    @MockBean
    private PermissionEvaluator permissionEvaluator;

    private static final UUID LEAD_ID = UUID.randomUUID();

    // ==================== Authentication Tests ====================

    @Test
    void getAll_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/leads"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/leads/" + LEAD_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_withoutAuth_returns401() throws Exception {
        CreateLeadRequest request = new CreateLeadRequest("Test lead", null, null, null);

        mockMvc.perform(post("/api/v1/leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Authorization Tests (UnauthorizedException -> 401 per GlobalExceptionHandler) ====================

    @Test
    void getAll_withoutPermission_returnsUnauthorized() throws Exception {
        when(permissionEvaluator.hasPermission("leads", "leer")).thenReturn(false);

        mockMvc.perform(get("/api/v1/leads")
                        .with(withNoPermission()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_AUTH"));
    }

    @Test
    void create_withoutPermission_returnsUnauthorized() throws Exception {
        when(permissionEvaluator.hasPermission("leads", "crear")).thenReturn(false);

        CreateLeadRequest request = new CreateLeadRequest("Test lead", null, null, null);

        mockMvc.perform(post("/api/v1/leads")
                        .with(withNoPermission())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_AUTH"));
    }

    // ==================== Success Tests ====================

    @Test
    void getAll_withPermission_returns200WithPagination() throws Exception {
        when(permissionEvaluator.hasPermission("leads", "leer")).thenReturn(true);

        Lead lead = createTestLead();
        Page<Lead> page = new PageImpl<>(List.of(lead));
        when(leadService.getAll(any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/leads")
                        .with(withPermission("leads"))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(LEAD_ID.toString()))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(1));
    }

    @Test
    void getById_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("leads", "leer")).thenReturn(true);

        Lead lead = createTestLead();
        when(leadService.getById(LEAD_ID)).thenReturn(lead);

        mockMvc.perform(get("/api/v1/leads/" + LEAD_ID)
                        .with(withPermission("leads")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(LEAD_ID.toString()))
                .andExpect(jsonPath("$.data.descripcion").value("Test Lead"));
    }

    @Test
    void create_withValidData_returns201() throws Exception {
        when(permissionEvaluator.hasPermission("leads", "crear")).thenReturn(true);

        Lead lead = createTestLead();
        when(leadService.create(any(CreateLeadRequest.class))).thenReturn(lead);

        CreateLeadRequest request = new CreateLeadRequest("Test Lead", null, null, null);

        mockMvc.perform(post("/api/v1/leads")
                        .with(withPermission("leads"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(LEAD_ID.toString()))
                .andExpect(jsonPath("$.message").value("Lead creado exitosamente"));
    }

    @Test
    void update_withValidData_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("leads", "editar")).thenReturn(true);

        Lead lead = createTestLead();
        when(leadService.update(eq(LEAD_ID), any(UpdateLeadRequest.class))).thenReturn(lead);

        UpdateLeadRequest request = new UpdateLeadRequest("Updated Lead", null, null, null);

        mockMvc.perform(put("/api/v1/leads/" + LEAD_ID)
                        .with(withPermission("leads"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lead actualizado exitosamente"));
    }

    @Test
    void delete_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("leads", "eliminar")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/leads/" + LEAD_ID)
                        .with(withPermission("leads")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Lead eliminado exitosamente"));
    }

    @Test
    void getEstadisticas_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("leads", "leer")).thenReturn(true);

        Map<String, Long> estadisticas = Map.of("nuevo", 5L, "en_proceso", 3L);
        when(leadService.getEstadisticas()).thenReturn(estadisticas);

        mockMvc.perform(get("/api/v1/leads/estadisticas")
                        .with(withPermission("leads")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.estadisticas.nuevo").value(5));
    }

    // ==================== Validation Tests ====================

    @Test
    void create_withBlankDescripcion_returns400() throws Exception {
        when(permissionEvaluator.hasPermission("leads", "crear")).thenReturn(true);

        CreateLeadRequest request = new CreateLeadRequest("", null, null, null);

        mockMvc.perform(post("/api/v1/leads")
                        .with(withPermission("leads"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION"));
    }

    // ==================== Not Found Tests ====================

    @Test
    void getById_notFound_returns404() throws Exception {
        when(permissionEvaluator.hasPermission("leads", "leer")).thenReturn(true);

        UUID nonExistentId = UUID.randomUUID();
        when(leadService.getById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Lead no encontrado"));

        mockMvc.perform(get("/api/v1/leads/" + nonExistentId)
                        .with(withPermission("leads")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_NOT_FOUND"));
    }

    // ==================== Pagination Tests ====================

    @Test
    void getAll_withPaginationParams_respectsPageAndSize() throws Exception {
        when(permissionEvaluator.hasPermission("leads", "leer")).thenReturn(true);

        Page<Lead> emptyPage = Page.empty();
        when(leadService.getAll(any(), any(), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/leads")
                        .with(withPermission("leads"))
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ==================== Helpers ====================

    private Lead createTestLead() {
        Lead lead = new Lead();
        lead.setId(LEAD_ID);
        lead.setDescripcion("Test Lead");
        lead.setEstadoId(UUID.randomUUID());
        lead.setActivo(true);
        lead.setCreatedAt(OffsetDateTime.now());
        return lead;
    }
}
