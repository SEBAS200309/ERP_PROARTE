package com.proarte.erp.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de integración para la función fn_crear_evento_desde_cotizacion.
 * Verifica que solo se crean eventos a partir de cotizaciones aprobadas
 * y que se lanza excepción cuando el estado no es 'aprobada'.
 */
@DisplayName("Evento Creación Integration Test")
class EventoCreacionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private UUID estadoAprobadaId;
    private UUID estadoBorradorId;

    @BeforeEach
    void setUp() {
        estadoAprobadaId = jdbcTemplate.queryForObject(
                "SELECT id FROM estado WHERE nombre = 'aprobada' AND contexto = 'cotizacion'",
                UUID.class
        );
        estadoBorradorId = jdbcTemplate.queryForObject(
                "SELECT id FROM estado WHERE nombre = 'borrador' AND contexto = 'cotizacion'",
                UUID.class
        );
    }

    @Test
    @DisplayName("fn_crear_evento_desde_cotizacion crea evento cuando estado es 'aprobada'")
    void debeCrearEventoCuandoCotizacionEstaAprobada() {
        UUID cotizacionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO cotizacion (id, codigo, estado_id, total, activo) VALUES (?, ?, ?, 50000, true)",
                cotizacionId, "COT-2024-EVT1", estadoAprobadaId
        );

        UUID eventoId = jdbcTemplate.queryForObject(
                "SELECT fn_crear_evento_desde_cotizacion(?)",
                UUID.class, cotizacionId
        );

        assertThat(eventoId).isNotNull();

        // Verificar que el evento fue creado con datos correctos
        String nombre = jdbcTemplate.queryForObject(
                "SELECT nombre FROM evento WHERE id = ?",
                String.class, eventoId
        );
        assertThat(nombre).isEqualTo("Evento - COT-2024-EVT1");

        // Verificar que tiene la cotizacion_id correcta
        UUID cotIdEnEvento = jdbcTemplate.queryForObject(
                "SELECT cotizacion_id FROM evento WHERE id = ?",
                UUID.class, eventoId
        );
        assertThat(cotIdEnEvento).isEqualTo(cotizacionId);
    }

    @Test
    @DisplayName("fn_crear_evento_desde_cotizacion lanza excepción cuando estado es 'borrador'")
    void debeLanzarExcepcionCuandoEstadoNoEsAprobada() {
        UUID cotizacionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO cotizacion (id, codigo, estado_id, total, activo) VALUES (?, ?, ?, 0, true)",
                cotizacionId, "COT-2024-EVT2", estadoBorradorId
        );

        assertThatThrownBy(() ->
                jdbcTemplate.queryForObject(
                        "SELECT fn_crear_evento_desde_cotizacion(?)",
                        UUID.class, cotizacionId
                )
        ).hasMessageContaining("La cotización debe estar aprobada para crear un evento");
    }

    @Test
    @DisplayName("El evento creado tiene estado_id de 'planificacion'")
    void eventoDebeCrearseConEstadoPlanificacion() {
        UUID cotizacionId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO cotizacion (id, codigo, estado_id, total, activo) VALUES (?, ?, ?, 30000, true)",
                cotizacionId, "COT-2024-EVT3", estadoAprobadaId
        );

        UUID eventoId = jdbcTemplate.queryForObject(
                "SELECT fn_crear_evento_desde_cotizacion(?)",
                UUID.class, cotizacionId
        );

        UUID estadoPlanificacionId = jdbcTemplate.queryForObject(
                "SELECT id FROM estado WHERE nombre = 'planificacion' AND contexto = 'evento'",
                UUID.class
        );

        UUID estadoIdEvento = jdbcTemplate.queryForObject(
                "SELECT estado_id FROM evento WHERE id = ?",
                UUID.class, eventoId
        );
        assertThat(estadoIdEvento).isEqualTo(estadoPlanificacionId);
    }
}
