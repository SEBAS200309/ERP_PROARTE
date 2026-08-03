package com.proarte.erp.inventario.controller;

import com.proarte.erp.controller.BaseControllerTest;
import com.proarte.erp.controller.TestSecurityConfig;
import com.proarte.erp.exception.GlobalExceptionHandler;
import com.proarte.erp.exception.InsufficientStockException;
import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.inventario.dto.CreateInsumoRequest;
import com.proarte.erp.inventario.dto.CreateMovimientoRequest;
import com.proarte.erp.inventario.entity.Insumo;
import com.proarte.erp.inventario.entity.InsumoMovimiento;
import com.proarte.erp.inventario.service.InventarioService;
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
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = InventarioController.class, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class InventarioControllerTest extends BaseControllerTest {

    @MockBean
    private InventarioService inventarioService;

    @MockBean
    private PermissionEvaluator permissionEvaluator;

    private static final UUID INSUMO_ID = UUID.randomUUID();
    private static final UUID MOVIMIENTO_ID = UUID.randomUUID();

    // ==================== Authentication Tests ====================

    @Test
    void getAll_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/inventario"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_withoutAuth_returns401() throws Exception {
        CreateInsumoRequest request = new CreateInsumoRequest("Cables XLR", "Cables audio", null);

        mockMvc.perform(post("/api/v1/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Authorization Tests ====================

    @Test
    void getAll_withoutPermission_returnsUnauthorized() throws Exception {
        when(permissionEvaluator.hasPermission("inventario", "leer")).thenReturn(false);

        mockMvc.perform(get("/api/v1/inventario")
                        .with(withNoPermission()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_AUTH"));
    }

    @Test
    void create_withoutPermission_returnsUnauthorized() throws Exception {
        when(permissionEvaluator.hasPermission("inventario", "crear")).thenReturn(false);

        CreateInsumoRequest request = new CreateInsumoRequest("Cables XLR", "Cables audio", null);

        mockMvc.perform(post("/api/v1/inventario")
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
        when(permissionEvaluator.hasPermission("inventario", "leer")).thenReturn(true);

        Insumo insumo = createTestInsumo();
        Page<Insumo> page = new PageImpl<>(List.of(insumo));
        when(inventarioService.getAll(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/inventario")
                        .with(withPermission("inventario"))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(INSUMO_ID.toString()))
                .andExpect(jsonPath("$.data.content[0].nombre").value("Cables XLR"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getById_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("inventario", "leer")).thenReturn(true);

        Insumo insumo = createTestInsumo();
        when(inventarioService.getById(INSUMO_ID)).thenReturn(insumo);

        mockMvc.perform(get("/api/v1/inventario/" + INSUMO_ID)
                        .with(withPermission("inventario")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(INSUMO_ID.toString()))
                .andExpect(jsonPath("$.data.nombre").value("Cables XLR"));
    }

    @Test
    void create_withValidData_returns201() throws Exception {
        when(permissionEvaluator.hasPermission("inventario", "crear")).thenReturn(true);

        Insumo insumo = createTestInsumo();
        when(inventarioService.createInsumo(any(CreateInsumoRequest.class))).thenReturn(insumo);

        CreateInsumoRequest request = new CreateInsumoRequest("Cables XLR", "Cables de audio profesional", null);

        mockMvc.perform(post("/api/v1/inventario")
                        .with(withPermission("inventario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(INSUMO_ID.toString()))
                .andExpect(jsonPath("$.message").value("Insumo creado exitosamente"));
    }

    @Test
    void registrarIngreso_withValidData_returns201() throws Exception {
        when(permissionEvaluator.hasPermission("inventario", "crear")).thenReturn(true);

        InsumoMovimiento movimiento = createTestMovimiento("INGRESO");
        when(inventarioService.registrarIngreso(any(CreateMovimientoRequest.class))).thenReturn(movimiento);

        CreateMovimientoRequest request = new CreateMovimientoRequest(INSUMO_ID, BigDecimal.TEN, "Compra de stock");

        mockMvc.perform(post("/api/v1/inventario/ingresos")
                        .with(withPermission("inventario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tipoMovimiento").value("INGRESO"))
                .andExpect(jsonPath("$.message").value("Ingreso registrado exitosamente"));
    }

    @Test
    void registrarRetiro_withValidData_returns201() throws Exception {
        when(permissionEvaluator.hasPermission("inventario", "crear")).thenReturn(true);

        InsumoMovimiento movimiento = createTestMovimiento("RETIRO");
        when(inventarioService.registrarRetiro(any(CreateMovimientoRequest.class))).thenReturn(movimiento);

        CreateMovimientoRequest request = new CreateMovimientoRequest(INSUMO_ID, BigDecimal.valueOf(5), "Uso en evento");

        mockMvc.perform(post("/api/v1/inventario/retiros")
                        .with(withPermission("inventario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tipoMovimiento").value("RETIRO"))
                .andExpect(jsonPath("$.message").value("Retiro registrado exitosamente"));
    }

    @Test
    void getMovimientos_withPermission_returns200() throws Exception {
        when(permissionEvaluator.hasPermission("inventario", "leer")).thenReturn(true);

        InsumoMovimiento movimiento = createTestMovimiento("INGRESO");
        Page<InsumoMovimiento> page = new PageImpl<>(List.of(movimiento));
        when(inventarioService.getMovimientos(eq(INSUMO_ID), any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/inventario/" + INSUMO_ID + "/movimientos")
                        .with(withPermission("inventario")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].tipoMovimiento").value("INGRESO"));
    }

    // ==================== Validation Tests ====================

    @Test
    void create_withBlankNombre_returns400() throws Exception {
        when(permissionEvaluator.hasPermission("inventario", "crear")).thenReturn(true);

        CreateInsumoRequest request = new CreateInsumoRequest("", "Descripcion", null);

        mockMvc.perform(post("/api/v1/inventario")
                        .with(withPermission("inventario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION"));
    }

    @Test
    void registrarIngreso_withNullCantidad_returns400() throws Exception {
        when(permissionEvaluator.hasPermission("inventario", "crear")).thenReturn(true);

        String invalidJson = """
                {"insumoId": "%s", "cantidad": null, "motivo": "test"}
                """.formatted(INSUMO_ID);

        mockMvc.perform(post("/api/v1/inventario/ingresos")
                        .with(withPermission("inventario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_VALIDATION"));
    }

    // ==================== Stock Exception Tests ====================

    @Test
    void registrarRetiro_withInsufficientStock_returns409() throws Exception {
        when(permissionEvaluator.hasPermission("inventario", "crear")).thenReturn(true);

        when(inventarioService.registrarRetiro(any(CreateMovimientoRequest.class)))
                .thenThrow(new InsufficientStockException("No hay suficiente stock de 'Cables XLR'"));

        CreateMovimientoRequest request = new CreateMovimientoRequest(INSUMO_ID, BigDecimal.valueOf(100), "Evento grande");

        mockMvc.perform(post("/api/v1/inventario/retiros")
                        .with(withPermission("inventario"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_STOCK"));
    }

    // ==================== Not Found Tests ====================

    @Test
    void getById_notFound_returns404() throws Exception {
        when(permissionEvaluator.hasPermission("inventario", "leer")).thenReturn(true);

        UUID nonExistentId = UUID.randomUUID();
        when(inventarioService.getById(nonExistentId))
                .thenThrow(new ResourceNotFoundException("Insumo no encontrado"));

        mockMvc.perform(get("/api/v1/inventario/" + nonExistentId)
                        .with(withPermission("inventario")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ERR_NOT_FOUND"));
    }

    // ==================== Helpers ====================

    private Insumo createTestInsumo() {
        Insumo insumo = new Insumo();
        insumo.setId(INSUMO_ID);
        insumo.setNombre("Cables XLR");
        insumo.setDescripcion("Cables de audio profesional");
        insumo.setStockActual(BigDecimal.valueOf(50));
        insumo.setCreatedAt(OffsetDateTime.now());
        insumo.setUpdatedAt(OffsetDateTime.now());
        return insumo;
    }

    private InsumoMovimiento createTestMovimiento(String tipo) {
        InsumoMovimiento movimiento = new InsumoMovimiento();
        movimiento.setId(MOVIMIENTO_ID);
        movimiento.setInsumoId(INSUMO_ID);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(BigDecimal.TEN);
        movimiento.setFecha(OffsetDateTime.now());
        movimiento.setMotivo("Test motivo");
        movimiento.setCreatedBy(TEST_USER_ID);
        return movimiento;
    }
}
