package com.proarte.erp.cotizaciones.controller;

import com.proarte.erp.controller.BaseControllerTest;
import com.proarte.erp.controller.TestSecurityConfig;
import com.proarte.erp.cotizaciones.dto.CambiarEstadoRequest;
import com.proarte.erp.cotizaciones.dto.CreateCotizacionRequest;
import com.proarte.erp.cotizaciones.entity.Cotizacion;
import com.proarte.erp.cotizaciones.service.CotizacionPdfService;
import com.proarte.erp.cotizaciones.service.CotizacionService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = CotizacionController.class, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class CotizacionControllerTest extends BaseControllerTest {

    @MockBean
    private CotizacionService cotizacionService;

    @MockBean
    private CotizacionPdfService cotizacionPdfService;

    @MockBean
    private PermissionEvaluator permissionEvaluator;

    private static final UUID COTIZACION_ID = UUID.randomUUID();
    private static final UUID ESTADO_ID = UUID.randomUUID();

    // ==================== Authentication Tests ====================

    @Test
    void getAll_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/cotizaciones"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_withoutAuth_returns401() throws Exception {
        CreateCotizacionRequest request = new CreateCotizacionRequest(
                "COT-001", ESTADO_ID, LocalDate.now().plusDays(30), null, null, null);

        mockMvc.perform(post("/api/v1/cotizaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Authorization Tests ====================

    @Test
    void getAll_withoutPermission_returnsUnauthorized() throws Exception {
        when(permissionEvaluator.hasPermission("cotizaciones", "leer")).thenReturn(false);

        mockMvc.perform(get("/api/v1/cotizaciones")
                        .with(withNoPermission()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_AUTH"));
    }

    // ==================== Success Tests ====================

    @Test
    void getAll_withPermission_returns200WithPagination() throws Exception {
        when(permissionEvaluator.hasPermission("cotizaciones", "leer")).thenReturn(true);

        Cotizacion cotizacion = createTestCotizacion();
        Page<Cotizacion> page = new PageImpl<>(List.of(cotizacion));
        when(cotizacionService.getAll(any(), any(), any(), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/cotizaciones")
                        .with(withPermission("cotizaciones"))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.page").value(0));
    }

    @Test
    void getById_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("cotizaciones", "leer")).thenReturn(true);

        Cotizacion cotizacion = createTestCotizacion();
        when(cotizacionService.getById(COTIZACION_ID)).thenReturn(cotizacion);

        mockMvc.perform(get("/api/v1/cotizaciones/" + COTIZACION_ID)
                        .with(withPermission("cotizaciones")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(COTIZACION_ID.toString()))
                .andExpect(jsonPath("$.data.codigo").value("COT-001"));
    }

    @Test
    void create_withValidData_returns201() throws Exception {
        when(permissionEvaluator.hasPermission("cotizaciones", "crear")).thenReturn(true);

        Cotizacion cotizacion = createTestCotizacion();
        when(cotizacionService.create(any(CreateCotizacionRequest.class))).thenReturn(cotizacion);

        CreateCotizacionRequest request = new CreateCotizacionRequest(
                "COT-001", ESTADO_ID, LocalDate.now().plusDays(30), null, null, null);

        mockMvc.perform(post("/api/v1/cotizaciones")
                        .with(withPermission("cotizaciones"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cotización creada exitosamente"));
    }

    @Test
    void delete_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("cotizaciones", "eliminar")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/cotizaciones/" + COTIZACION_ID)
                        .with(withPermission("cotizaciones")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cotización eliminada exitosamente"));
    }

    // ==================== Special Endpoints ====================

    @Test
    void cambiarEstado_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("cotizaciones", "editar")).thenReturn(true);

        Cotizacion cotizacion = createTestCotizacion();
        when(cotizacionService.cambiarEstado(eq(COTIZACION_ID), any(CambiarEstadoRequest.class))).thenReturn(cotizacion);

        CambiarEstadoRequest request = new CambiarEstadoRequest(UUID.randomUUID());

        mockMvc.perform(patch("/api/v1/cotizaciones/" + COTIZACION_ID + "/estado")
                        .with(withPermission("cotizaciones"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Estado actualizado exitosamente"));
    }

    @Test
    void getPorVencer_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("cotizaciones", "leer")).thenReturn(true);

        Cotizacion cotizacion = createTestCotizacion();
        when(cotizacionService.getPorVencer(7)).thenReturn(List.of(cotizacion));

        mockMvc.perform(get("/api/v1/cotizaciones/vencimientos")
                        .with(withPermission("cotizaciones"))
                        .param("dias", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].codigo").value("COT-001"));
    }

    @Test
    void recalcularTotal_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("cotizaciones", "editar")).thenReturn(true);

        Cotizacion cotizacion = createTestCotizacion();
        when(cotizacionService.getById(COTIZACION_ID)).thenReturn(cotizacion);

        mockMvc.perform(post("/api/v1/cotizaciones/" + COTIZACION_ID + "/recalcular")
                        .with(withPermission("cotizaciones")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Total recalculado exitosamente"));
    }

    @Test
    void generatePdf_withPermission_returnsPdf() throws Exception {
        when(permissionEvaluator.hasPermission("cotizaciones", "leer")).thenReturn(true);

        Cotizacion cotizacion = createTestCotizacion();
        when(cotizacionService.getById(COTIZACION_ID)).thenReturn(cotizacion);
        when(cotizacionPdfService.generatePdf(cotizacion)).thenReturn(new byte[]{1, 2, 3, 4});

        mockMvc.perform(get("/api/v1/cotizaciones/" + COTIZACION_ID + "/pdf")
                        .with(withPermission("cotizaciones")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    // ==================== Validation Tests ====================

    @Test
    void create_withNullEstadoId_returns400() throws Exception {
        when(permissionEvaluator.hasPermission("cotizaciones", "crear")).thenReturn(true);

        String invalidJson = """
                {"codigo": "COT-001", "estadoId": null}
                """;

        mockMvc.perform(post("/api/v1/cotizaciones")
                        .with(withPermission("cotizaciones"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION"));
    }

    // ==================== Not Found Tests ====================

    @Test
    void getById_notFound_returns404() throws Exception {
        when(permissionEvaluator.hasPermission("cotizaciones", "leer")).thenReturn(true);

        UUID nonExistentId = UUID.randomUUID();
        when(cotizacionService.getById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Cotización no encontrada"));

        mockMvc.perform(get("/api/v1/cotizaciones/" + nonExistentId)
                        .with(withPermission("cotizaciones")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_NOT_FOUND"));
    }

    // ==================== Helpers ====================

    private Cotizacion createTestCotizacion() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setId(COTIZACION_ID);
        cotizacion.setCodigo("COT-001");
        cotizacion.setEstadoId(ESTADO_ID);
        cotizacion.setFechaVencimiento(LocalDate.now().plusDays(30));
        cotizacion.setTotal(BigDecimal.valueOf(1000));
        cotizacion.setItems(List.of());
        cotizacion.setCreatedAt(OffsetDateTime.now());
        return cotizacion;
    }
}
