package com.proarte.erp.integration;

import com.proarte.erp.cotizaciones.entity.Cotizacion;
import com.proarte.erp.cotizaciones.repository.CotizacionItemRepository;
import com.proarte.erp.cotizaciones.repository.CotizacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para CotizacionRepository y triggers asociados:
 * - trg_calcular_subtotal_item (calcula subtotal en cotizacion_item)
 * - trg_recalcular_total (actualiza cotizacion.total al modificar items)
 * - fn_recalcular_total_cotizacion (función llamada directamente)
 */
@DisplayName("Cotizacion Repository Integration Test")
class CotizacionRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CotizacionRepository cotizacionRepository;

    @Autowired
    private CotizacionItemRepository cotizacionItemRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID estadoBorradorId;
    private UUID estadoAprobadaId;
    private UUID servicioId;

    @BeforeEach
    void setUp() {
        // Obtener estado 'borrador' del catálogo
        estadoBorradorId = jdbcTemplate.queryForObject(
                "SELECT id FROM estado WHERE nombre = 'borrador' AND contexto = 'cotizacion'",
                UUID.class
        );
        estadoAprobadaId = jdbcTemplate.queryForObject(
                "SELECT id FROM estado WHERE nombre = 'aprobada' AND contexto = 'cotizacion'",
                UUID.class
        );

        // Crear un servicio de prueba
        servicioId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO servicio (id, nombre, activo) VALUES (?, 'Servicio Test', true)",
                servicioId
        );
    }

    @Test
    @DisplayName("Debe crear y recuperar una cotización correctamente")
    @Transactional
    void debeCrearYRecuperarCotizacion() {
        Cotizacion cotizacion = Cotizacion.builder()
                .codigo("COT-2024-001")
                .estadoId(estadoBorradorId)
                .total(BigDecimal.ZERO)
                .build();

        Cotizacion guardada = cotizacionRepository.save(cotizacion);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getCodigo()).isEqualTo("COT-2024-001");
        assertThat(guardada.getEstadoId()).isEqualTo(estadoBorradorId);
    }

    @Test
    @DisplayName("searchByCodigo debe encontrar cotizaciones por código parcial")
    @Transactional
    void debeEncontrarPorCodigoParcial() {
        Cotizacion cot1 = Cotizacion.builder()
                .codigo("COT-2024-100")
                .estadoId(estadoBorradorId)
                .total(BigDecimal.ZERO)
                .build();
        Cotizacion cot2 = Cotizacion.builder()
                .codigo("COT-2024-200")
                .estadoId(estadoBorradorId)
                .total(BigDecimal.ZERO)
                .build();
        cotizacionRepository.save(cot1);
        cotizacionRepository.save(cot2);

        var resultado = cotizacionRepository.searchByCodigo("2024-1", PageRequest.of(0, 10));

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getCodigo()).isEqualTo("COT-2024-100");
    }

    @Test
    @DisplayName("findByEstadoId debe retornar cotizaciones con estado específico")
    @Transactional
    void debeEncontrarPorEstadoId() {
        Cotizacion cot1 = Cotizacion.builder()
                .codigo("COT-2024-301")
                .estadoId(estadoBorradorId)
                .total(BigDecimal.ZERO)
                .build();
        Cotizacion cot2 = Cotizacion.builder()
                .codigo("COT-2024-302")
                .estadoId(estadoAprobadaId)
                .total(BigDecimal.ZERO)
                .build();
        cotizacionRepository.save(cot1);
        cotizacionRepository.save(cot2);

        var resultado = cotizacionRepository.findByEstadoId(estadoAprobadaId, PageRequest.of(0, 10));

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getCodigo()).isEqualTo("COT-2024-302");
    }

    @Test
    @DisplayName("El trigger trg_calcular_subtotal_item calcula subtotal al insertar item")
    void debeCalcularSubtotalAlInsertarItem() {
        // Crear cotización vía SQL directo para evitar conflictos con @Transactional y triggers
        UUID cotizacionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO cotizacion (id, codigo, estado_id, total, activo) VALUES (?, ?, ?, 0, true)",
                cotizacionId, "COT-2024-TRG1", estadoBorradorId
        );

        // Insertar item — el trigger debe calcular subtotal = cantidad * precio_unitario
        UUID itemId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)",
                itemId, cotizacionId, servicioId, 3, new BigDecimal("10000.00")
        );

        // Verificar que el trigger calculó el subtotal
        BigDecimal subtotal = jdbcTemplate.queryForObject(
                "SELECT subtotal FROM cotizacion_item WHERE id = ?",
                BigDecimal.class, itemId
        );
        assertThat(subtotal).isEqualByComparingTo(new BigDecimal("30000.00"));
    }

    @Test
    @DisplayName("El trigger trg_recalcular_total actualiza cotizacion.total tras agregar items")
    void debeRecalcularTotalAlAgregarItems() {
        UUID cotizacionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO cotizacion (id, codigo, estado_id, total, activo) VALUES (?, ?, ?, 0, true)",
                cotizacionId, "COT-2024-TRG2", estadoBorradorId
        );

        // Insertar dos items
        jdbcTemplate.update(
                "INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)",
                UUID.randomUUID(), cotizacionId, servicioId, 2, new BigDecimal("5000.00")
        );
        jdbcTemplate.update(
                "INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)",
                UUID.randomUUID(), cotizacionId, servicioId, 1, new BigDecimal("15000.00")
        );

        // Verificar que el total de la cotización se recalculó
        BigDecimal total = jdbcTemplate.queryForObject(
                "SELECT total FROM cotizacion WHERE id = ?",
                BigDecimal.class, cotizacionId
        );
        // 2*5000 + 1*15000 = 25000
        assertThat(total).isEqualByComparingTo(new BigDecimal("25000.00"));
    }

    @Test
    @DisplayName("fn_recalcular_total_cotizacion funciona al llamarse directamente")
    void debeRecalcularTotalConFuncionDirecta() {
        UUID cotizacionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO cotizacion (id, codigo, estado_id, total, activo) VALUES (?, ?, ?, 0, true)",
                cotizacionId, "COT-2024-FN1", estadoBorradorId
        );

        // Insertar items con subtotal manual (sin trigger, usando subtotal directo para simular)
        jdbcTemplate.update(
                "INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)",
                UUID.randomUUID(), cotizacionId, servicioId, 4, new BigDecimal("2500.00")
        );

        // Llamar a la función directamente
        BigDecimal resultado = jdbcTemplate.queryForObject(
                "SELECT fn_recalcular_total_cotizacion(?)",
                BigDecimal.class, cotizacionId
        );

        // El trigger ya habrá calculado subtotal = 4*2500 = 10000
        assertThat(resultado).isEqualByComparingTo(new BigDecimal("10000.00"));

        // Verificar que el total en la tabla también se actualizó
        BigDecimal totalEnTabla = jdbcTemplate.queryForObject(
                "SELECT total FROM cotizacion WHERE id = ?",
                BigDecimal.class, cotizacionId
        );
        assertThat(totalEnTabla).isEqualByComparingTo(new BigDecimal("10000.00"));
    }
}
