package com.proarte.erp.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración que verifican que las migraciones Flyway (V1-V10)
 * se ejecutan correctamente y crean las estructuras esperadas.
 */
@DisplayName("Flyway Migrations Integration Test")
class FlywayMigrationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Todas las migraciones V1-V10 se ejecutan exitosamente")
    void debeEjecutarTodasLasMigracionesExitosamente() {
        var migrationInfo = flyway.info();
        var applied = migrationInfo.applied();

        assertThat(applied).hasSizeGreaterThanOrEqualTo(10);
        assertThat(applied).allSatisfy(info ->
                assertThat(info.getState().isApplied()).isTrue()
        );
    }

    @Test
    @DisplayName("Las tablas principales existen después de la migración")
    void debeCrearTablasPrincipales() {
        List<String> tablasEsperadas = List.of(
                "usuario", "persona", "empresa", "servicio", "proveedor",
                "cotizacion", "cotizacion_item", "evento", "insumo",
                "insumo_movimiento", "descuento_recargo", "tipo_descuento_recargo",
                "estado", "rol", "lead"
        );

        for (String tabla : tablasEsperadas) {
            Boolean existe = jdbcTemplate.queryForObject(
                    "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)",
                    Boolean.class, tabla
            );
            assertThat(existe)
                    .as("La tabla '%s' debe existir", tabla)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Los triggers principales existen en la base de datos")
    void debeCrearTriggers() {
        List<String> triggersEsperados = List.of(
                "trg_actualizar_stock",
                "trg_calcular_subtotal_item",
                "trg_recalcular_total"
        );

        for (String trigger : triggersEsperados) {
            Boolean existe = jdbcTemplate.queryForObject(
                    "SELECT EXISTS (SELECT FROM information_schema.triggers WHERE trigger_name = ?)",
                    Boolean.class, trigger
            );
            assertThat(existe)
                    .as("El trigger '%s' debe existir", trigger)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Las funciones principales existen en la base de datos")
    void debeCrearFunciones() {
        List<String> funcionesEsperadas = List.of(
                "fn_recalcular_total_cotizacion",
                "fn_crear_evento_desde_cotizacion"
        );

        for (String funcion : funcionesEsperadas) {
            Boolean existe = jdbcTemplate.queryForObject(
                    "SELECT EXISTS (SELECT FROM information_schema.routines WHERE routine_name = ?)",
                    Boolean.class, funcion
            );
            assertThat(existe)
                    .as("La función '%s' debe existir", funcion)
                    .isTrue();
        }
    }
}
