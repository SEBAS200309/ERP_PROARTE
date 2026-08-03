package com.proarte.erp.integration;

import com.proarte.erp.inventario.entity.Insumo;
import com.proarte.erp.inventario.repository.InsumoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de integración para el comportamiento de soft-delete.
 * Verifica que @SQLRestriction("activo = true") filtra registros desactivados
 * y que softDelete/restore funcionan correctamente.
 */
@DisplayName("Soft Delete Integration Test")
class SoftDeleteIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Test
    @DisplayName("Después de softDelete, el registro no se retorna por findAll ni findById")
    @Transactional
    void debeOcultarRegistroDespuesDeSoftDelete() {
        // Crear insumo
        Insumo insumo = Insumo.builder()
                .nombre("Insumo Borrable")
                .stockActual(BigDecimal.TEN)
                .build();
        insumo.setActivo(true);
        Insumo guardado = insumoRepository.saveAndFlush(insumo);
        UUID insumoId = guardado.getId();

        // Verificar que está visible
        assertThat(insumoRepository.findById(insumoId)).isPresent();

        // Ejecutar soft delete
        insumoRepository.softDelete(insumoId);
        insumoRepository.flush();

        // Limpiar cache de primer nivel para forzar re-lectura de BD
        entityManager.clear();

        // Verificar que ya no es visible via JPA
        Optional<Insumo> resultado = insumoRepository.findById(insumoId);
        assertThat(resultado).isEmpty();

        // Verificar que no aparece en findAll
        assertThat(insumoRepository.findAll())
                .extracting(Insumo::getId)
                .doesNotContain(insumoId);
    }

    @Test
    @DisplayName("Después de softDelete, el registro aún existe en la BD (query nativa)")
    @Transactional
    void registroDebeExistirEnBdDespuesDeSoftDelete() {
        Insumo insumo = Insumo.builder()
                .nombre("Insumo Persistente")
                .stockActual(BigDecimal.valueOf(5))
                .build();
        insumo.setActivo(true);
        Insumo guardado = insumoRepository.saveAndFlush(insumo);
        UUID insumoId = guardado.getId();

        // Soft delete
        insumoRepository.softDelete(insumoId);
        insumoRepository.flush();

        // Verificar con query nativa que el registro existe pero con activo=false
        Boolean existe = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM insumo WHERE id = ? AND activo = false)",
                Boolean.class, insumoId
        );
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("Después de restore, el registro es visible de nuevo")
    @Transactional
    void debeRestaurarRegistroDespuesDeRestore() {
        Insumo insumo = Insumo.builder()
                .nombre("Insumo Restaurable")
                .stockActual(BigDecimal.valueOf(20))
                .build();
        insumo.setActivo(true);
        Insumo guardado = insumoRepository.saveAndFlush(insumo);
        UUID insumoId = guardado.getId();

        // Soft delete y luego restore
        insumoRepository.softDelete(insumoId);
        insumoRepository.flush();
        insumoRepository.restore(insumoId);
        insumoRepository.flush();

        entityManager.clear();

        // Verificar que es visible de nuevo
        Optional<Insumo> resultado = insumoRepository.findById(insumoId);
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Insumo Restaurable");
    }

    @Test
    @DisplayName("existsActiveById retorna false después de soft-delete y true después de restore")
    @Transactional
    void existsActiveByIdDebeFuncionarCorrectamente() {
        Insumo insumo = Insumo.builder()
                .nombre("Insumo Verificable")
                .stockActual(BigDecimal.ONE)
                .build();
        insumo.setActivo(true);
        Insumo guardado = insumoRepository.saveAndFlush(insumo);
        UUID insumoId = guardado.getId();

        // Debe existir activo
        assertThat(insumoRepository.existsActiveById(insumoId)).isTrue();

        // Soft delete
        insumoRepository.softDelete(insumoId);
        insumoRepository.flush();

        // Debe retornar false
        assertThat(insumoRepository.existsActiveById(insumoId)).isFalse();

        // Restore
        insumoRepository.restore(insumoId);
        insumoRepository.flush();

        // Debe retornar true de nuevo
        assertThat(insumoRepository.existsActiveById(insumoId)).isTrue();
    }
}
