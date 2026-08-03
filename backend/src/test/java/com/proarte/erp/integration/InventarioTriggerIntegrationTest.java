package com.proarte.erp.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración para el trigger trg_actualizar_stock en insumo_movimiento.
 * Verifica que el stock se actualiza correctamente en ingresos y retiros,
 * y que se lanza excepción cuando no hay suficiente stock para un retiro.
 */
@DisplayName("Inventario Trigger Integration Test")
class InventarioTriggerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID insumoId;

    @BeforeEach
    void setUp() {
        insumoId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO insumo (id, nombre, stock_actual, activo) VALUES (?, 'Insumo Test', 0, true)",
                insumoId
        );
    }

    @Test
    @DisplayName("trg_actualizar_stock incrementa stock en ingreso")
    void debeIncrementarStockEnIngreso() {
        jdbcTemplate.update(
                "INSERT INTO insumo_movimiento (id, insumo_id, tipo_movimiento, cantidad) VALUES (?, ?, 'ingreso', ?)",
                UUID.randomUUID(), insumoId, new BigDecimal("50.00")
        );

        BigDecimal stockActual = jdbcTemplate.queryForObject(
                "SELECT stock_actual FROM insumo WHERE id = ?",
                BigDecimal.class, insumoId
        );
        assertThat(stockActual).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("trg_actualizar_stock decrementa stock en retiro")
    void debeDecrementarStockEnRetiro() {
        // Primero ingresar stock
        jdbcTemplate.update(
                "INSERT INTO insumo_movimiento (id, insumo_id, tipo_movimiento, cantidad) VALUES (?, ?, 'ingreso', ?)",
                UUID.randomUUID(), insumoId, new BigDecimal("100.00")
        );

        // Luego retirar
        jdbcTemplate.update(
                "INSERT INTO insumo_movimiento (id, insumo_id, tipo_movimiento, cantidad) VALUES (?, ?, 'retiro', ?)",
                UUID.randomUUID(), insumoId, new BigDecimal("30.00")
        );

        BigDecimal stockActual = jdbcTemplate.queryForObject(
                "SELECT stock_actual FROM insumo WHERE id = ?",
                BigDecimal.class, insumoId
        );
        assertThat(stockActual).isEqualByComparingTo(new BigDecimal("70.00"));
    }

    @Test
    @DisplayName("trg_actualizar_stock lanza excepción cuando retiro supera stock disponible")
    void debeLanzarExcepcionCuandoRetiroSuperaStock() {
        // Ingresar solo 10 unidades
        jdbcTemplate.update(
                "INSERT INTO insumo_movimiento (id, insumo_id, tipo_movimiento, cantidad) VALUES (?, ?, 'ingreso', ?)",
                UUID.randomUUID(), insumoId, new BigDecimal("10.00")
        );

        // Intentar retirar más de lo disponible
        assertThatThrownBy(() ->
                jdbcTemplate.update(
                        "INSERT INTO insumo_movimiento (id, insumo_id, tipo_movimiento, cantidad) VALUES (?, ?, 'retiro', ?)",
                        UUID.randomUUID(), insumoId, new BigDecimal("20.00")
                )
        ).hasMessageContaining("No hay suficiente stock");
    }

    @Test
    @DisplayName("Múltiples ingresos y retiros acumulan stock correctamente")
    void debeAcumularMultiplesMovimientosCorrectamente() {
        // Ingreso 1: +50
        jdbcTemplate.update(
                "INSERT INTO insumo_movimiento (id, insumo_id, tipo_movimiento, cantidad) VALUES (?, ?, 'ingreso', ?)",
                UUID.randomUUID(), insumoId, new BigDecimal("50.00")
        );
        // Ingreso 2: +30
        jdbcTemplate.update(
                "INSERT INTO insumo_movimiento (id, insumo_id, tipo_movimiento, cantidad) VALUES (?, ?, 'ingreso', ?)",
                UUID.randomUUID(), insumoId, new BigDecimal("30.00")
        );
        // Retiro 1: -20
        jdbcTemplate.update(
                "INSERT INTO insumo_movimiento (id, insumo_id, tipo_movimiento, cantidad) VALUES (?, ?, 'retiro', ?)",
                UUID.randomUUID(), insumoId, new BigDecimal("20.00")
        );
        // Ingreso 3: +10
        jdbcTemplate.update(
                "INSERT INTO insumo_movimiento (id, insumo_id, tipo_movimiento, cantidad) VALUES (?, ?, 'ingreso', ?)",
                UUID.randomUUID(), insumoId, new BigDecimal("10.00")
        );
        // Retiro 2: -15
        jdbcTemplate.update(
                "INSERT INTO insumo_movimiento (id, insumo_id, tipo_movimiento, cantidad) VALUES (?, ?, 'retiro', ?)",
                UUID.randomUUID(), insumoId, new BigDecimal("15.00")
        );

        // Stock esperado: 50 + 30 - 20 + 10 - 15 = 55
        BigDecimal stockActual = jdbcTemplate.queryForObject(
                "SELECT stock_actual FROM insumo WHERE id = ?",
                BigDecimal.class, insumoId
        );
        assertThat(stockActual).isEqualByComparingTo(new BigDecimal("55.00"));
    }
}
