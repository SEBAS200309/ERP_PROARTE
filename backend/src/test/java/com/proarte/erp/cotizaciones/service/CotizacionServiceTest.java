package com.proarte.erp.cotizaciones.service;

import com.proarte.erp.cotizaciones.dto.*;
import com.proarte.erp.cotizaciones.entity.Cotizacion;
import com.proarte.erp.cotizaciones.repository.CotizacionItemRepository;
import com.proarte.erp.cotizaciones.repository.CotizacionRepository;
import com.proarte.erp.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CotizacionServiceTest {

    @Mock
    private CotizacionRepository cotizacionRepository;

    @Mock
    private CotizacionItemRepository cotizacionItemRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private CotizacionService cotizacionService;

    private Cotizacion createTestCotizacion() {
        Cotizacion cotizacion = Cotizacion.builder()
                .codigo("COT-2024-001")
                .estadoId(UUID.randomUUID())
                .total(BigDecimal.valueOf(1500))
                .fechaVencimiento(LocalDate.now().plusDays(30))
                .build();
        cotizacion.setId(UUID.randomUUID());
        cotizacion.setActivo(true);
        return cotizacion;
    }

    @Test
    @DisplayName("getAll retorna todas las cotizaciones sin filtros")
    void shouldReturnAll_whenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cotizacion> expectedPage = new PageImpl<>(List.of(createTestCotizacion()));
        when(cotizacionRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Cotizacion> result = cotizacionService.getAll(null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAll filtra por search cuando se proporciona")
    void shouldFilterBySearch_whenSearchProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cotizacion> expectedPage = new PageImpl<>(List.of(createTestCotizacion()));
        when(cotizacionRepository.searchByCodigo("COT-2024", pageable)).thenReturn(expectedPage);

        Page<Cotizacion> result = cotizacionService.getAll("COT-2024", null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(cotizacionRepository).searchByCodigo("COT-2024", pageable);
    }

    @Test
    @DisplayName("getAll filtra por estadoId cuando se proporciona")
    void shouldFilterByEstadoId() {
        Pageable pageable = PageRequest.of(0, 10);
        UUID estadoId = UUID.randomUUID();
        Page<Cotizacion> expectedPage = new PageImpl<>(List.of());
        when(cotizacionRepository.findByEstadoId(estadoId, pageable)).thenReturn(expectedPage);

        Page<Cotizacion> result = cotizacionService.getAll(null, estadoId, null, null, pageable);

        verify(cotizacionRepository).findByEstadoId(estadoId, pageable);
    }

    @Test
    @DisplayName("getById retorna cotizacion cuando existe")
    void shouldReturnCotizacion_whenIdExists() {
        Cotizacion cotizacion = createTestCotizacion();
        when(cotizacionRepository.findById(cotizacion.getId())).thenReturn(Optional.of(cotizacion));

        Cotizacion result = cotizacionService.getById(cotizacion.getId());

        assertThat(result.getCodigo()).isEqualTo("COT-2024-001");
    }

    @Test
    @DisplayName("getById lanza ResourceNotFoundException cuando no existe")
    void shouldThrowNotFound_whenCotizacionDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(cotizacionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cotizacionService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Cotizacion");
    }

    @Test
    @DisplayName("create genera codigo automaticamente cuando no se proporciona")
    void shouldGenerateCodigo_whenNotProvided() {
        UUID estadoId = UUID.randomUUID();
        CreateCotizacionRequest request = new CreateCotizacionRequest(
                null, estadoId, LocalDate.now().plusDays(30), null, null, null
        );

        when(cotizacionRepository.findMaxCodigoSequence(anyString())).thenReturn(5);
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(inv -> {
            Cotizacion c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        Cotizacion result = cotizacionService.create(request);

        assertThat(result.getCodigo()).startsWith("COT-").contains("-006");
    }

    @Test
    @DisplayName("create usa codigo proporcionado si no es null ni vacio")
    void shouldUseCodigo_whenProvided() {
        UUID estadoId = UUID.randomUUID();
        CreateCotizacionRequest request = new CreateCotizacionRequest(
                "COT-CUSTOM-001", estadoId, null, null, null, null
        );

        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(inv -> {
            Cotizacion c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        Cotizacion result = cotizacionService.create(request);

        assertThat(result.getCodigo()).isEqualTo("COT-CUSTOM-001");
    }

    @Test
    @DisplayName("create guarda items y recalcula total cuando items no es null")
    void shouldSaveItemsAndRecalculate_whenItemsProvided() {
        UUID estadoId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        CotizacionItemRequest item = new CotizacionItemRequest(servicioId, 2, BigDecimal.valueOf(100), null);
        CreateCotizacionRequest request = new CreateCotizacionRequest(
                "COT-2024-010", estadoId, null, null, null, List.of(item)
        );

        UUID cotizacionId = UUID.randomUUID();
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(inv -> {
            Cotizacion c = inv.getArgument(0);
            c.setId(cotizacionId);
            return c;
        });
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), any()))
                .thenReturn(BigDecimal.valueOf(200));
        Cotizacion refreshed = createTestCotizacion();
        refreshed.setId(cotizacionId);
        refreshed.setTotal(BigDecimal.valueOf(200));
        when(cotizacionRepository.findById(cotizacionId)).thenReturn(Optional.of(refreshed));

        Cotizacion result = cotizacionService.create(request);

        verify(cotizacionItemRepository).saveAll(anyList());
        verify(jdbcTemplate).queryForObject(eq("SELECT fn_recalcular_total_cotizacion(?)"), eq(BigDecimal.class), eq(cotizacionId));
        assertThat(result.getTotal()).isEqualByComparingTo(BigDecimal.valueOf(200));
    }

    @Test
    @DisplayName("update actualiza campos y recalcula total cuando items proporcionados")
    void shouldUpdateFieldsAndRecalculate_whenItemsProvided() {
        UUID id = UUID.randomUUID();
        Cotizacion existing = createTestCotizacion();
        existing.setId(id);

        UUID newEstadoId = UUID.randomUUID();
        CotizacionItemRequest item = new CotizacionItemRequest(UUID.randomUUID(), 1, BigDecimal.TEN, null);
        UpdateCotizacionRequest request = new UpdateCotizacionRequest(newEstadoId, null, null, null, List.of(item));

        when(cotizacionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jdbcTemplate.queryForObject(anyString(), eq(BigDecimal.class), any())).thenReturn(BigDecimal.TEN);

        // Return updated cotizacion on second findById
        Cotizacion updated = createTestCotizacion();
        updated.setId(id);
        updated.setEstadoId(newEstadoId);
        updated.setTotal(BigDecimal.TEN);
        when(cotizacionRepository.findById(id)).thenReturn(Optional.of(existing)).thenReturn(Optional.of(updated));

        Cotizacion result = cotizacionService.update(id, request);

        verify(cotizacionItemRepository).deleteByCotizacionId(id);
        verify(cotizacionItemRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("delete realiza soft-delete cuando cotizacion existe")
    void shouldSoftDelete_whenCotizacionExists() {
        UUID id = UUID.randomUUID();
        when(cotizacionRepository.existsActiveById(id)).thenReturn(true);

        cotizacionService.delete(id);

        verify(cotizacionRepository).softDelete(id);
    }

    @Test
    @DisplayName("delete lanza ResourceNotFoundException cuando cotizacion no existe")
    void shouldThrowNotFound_whenDeleteNonExistentCotizacion() {
        UUID id = UUID.randomUUID();
        when(cotizacionRepository.existsActiveById(id)).thenReturn(false);

        assertThatThrownBy(() -> cotizacionService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("cambiarEstado actualiza el estado de la cotizacion")
    void shouldChangeEstado() {
        UUID id = UUID.randomUUID();
        UUID newEstadoId = UUID.randomUUID();
        Cotizacion cotizacion = createTestCotizacion();
        cotizacion.setId(id);

        when(cotizacionRepository.findById(id)).thenReturn(Optional.of(cotizacion));
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Cotizacion result = cotizacionService.cambiarEstado(id, new CambiarEstadoRequest(newEstadoId));

        assertThat(result.getEstadoId()).isEqualTo(newEstadoId);
    }

    @Test
    @DisplayName("getPorVencer usa 7 dias por defecto cuando dias es null")
    void shouldUseDefaultDays_whenDiasIsNull() {
        when(cotizacionRepository.findPorVencer(7)).thenReturn(List.of());

        List<Cotizacion> result = cotizacionService.getPorVencer(null);

        verify(cotizacionRepository).findPorVencer(7);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getPorVencer usa 7 dias por defecto cuando dias es negativo")
    void shouldUseDefaultDays_whenDiasIsNegative() {
        when(cotizacionRepository.findPorVencer(7)).thenReturn(List.of());

        cotizacionService.getPorVencer(-5);

        verify(cotizacionRepository).findPorVencer(7);
    }

    @Test
    @DisplayName("recalcularTotal llama a funcion PostgreSQL via JdbcTemplate")
    void shouldCallPostgresFunction() {
        UUID cotizacionId = UUID.randomUUID();
        when(jdbcTemplate.queryForObject(
                eq("SELECT fn_recalcular_total_cotizacion(?)"),
                eq(BigDecimal.class),
                eq(cotizacionId)
        )).thenReturn(BigDecimal.valueOf(5000));

        BigDecimal result = cotizacionService.recalcularTotal(cotizacionId);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }
}
