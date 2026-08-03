package com.proarte.erp.inventario.service;

import com.proarte.erp.exception.InsufficientStockException;
import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.inventario.dto.CreateInsumoRequest;
import com.proarte.erp.inventario.dto.CreateMovimientoRequest;
import com.proarte.erp.inventario.entity.Insumo;
import com.proarte.erp.inventario.entity.InsumoMovimiento;
import com.proarte.erp.inventario.repository.InsumoMovimientoRepository;
import com.proarte.erp.inventario.repository.InsumoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InsumoRepository insumoRepository;

    @Mock
    private InsumoMovimientoRepository movimientoRepository;

    @InjectMocks
    private InventarioService inventarioService;

    private Insumo createTestInsumo() {
        Insumo insumo = Insumo.builder()
                .nombre("Cables XLR")
                .descripcion("Cable XLR 5m")
                .stockActual(BigDecimal.valueOf(20))
                .build();
        insumo.setId(UUID.randomUUID());
        insumo.setActivo(true);
        return insumo;
    }

    @Test
    @DisplayName("getAll retorna todos los insumos sin busqueda")
    void shouldReturnAll_whenNoSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Insumo> expectedPage = new PageImpl<>(List.of(createTestInsumo()));
        when(insumoRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Insumo> result = inventarioService.getAll(null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAll filtra por nombre cuando search no es null")
    void shouldFilterByNombre_whenSearchProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Insumo> expectedPage = new PageImpl<>(List.of(createTestInsumo()));
        when(insumoRepository.searchByNombre("Cable", pageable)).thenReturn(expectedPage);

        Page<Insumo> result = inventarioService.getAll("Cable", pageable);

        verify(insumoRepository).searchByNombre("Cable", pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getById retorna insumo cuando existe")
    void shouldReturnInsumo_whenIdExists() {
        Insumo insumo = createTestInsumo();
        when(insumoRepository.findById(insumo.getId())).thenReturn(Optional.of(insumo));

        Insumo result = inventarioService.getById(insumo.getId());

        assertThat(result.getNombre()).isEqualTo("Cables XLR");
    }

    @Test
    @DisplayName("getById lanza ResourceNotFoundException cuando no existe")
    void shouldThrowNotFound_whenInsumoDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(insumoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventarioService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Insumo");
    }

    @Test
    @DisplayName("createInsumo crea insumo con stock inicial en cero")
    void shouldCreateInsumo_withZeroStock() {
        CreateInsumoRequest request = new CreateInsumoRequest("Microfono SM58", "Shure SM58", UUID.randomUUID());

        when(insumoRepository.save(any(Insumo.class))).thenAnswer(inv -> {
            Insumo i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        Insumo result = inventarioService.createInsumo(request);

        assertThat(result.getNombre()).isEqualTo("Microfono SM58");
        assertThat(result.getStockActual()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getActivo()).isTrue();
    }

    @Test
    @DisplayName("registrarIngreso crea movimiento de ingreso")
    void shouldRegisterIngreso() {
        UUID insumoId = UUID.randomUUID();
        Insumo insumo = createTestInsumo();
        insumo.setId(insumoId);
        CreateMovimientoRequest request = new CreateMovimientoRequest(insumoId, BigDecimal.valueOf(5), "Compra");

        when(insumoRepository.findById(insumoId)).thenReturn(Optional.of(insumo));
        when(movimientoRepository.save(any(InsumoMovimiento.class))).thenAnswer(inv -> {
            InsumoMovimiento m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        InsumoMovimiento result = inventarioService.registrarIngreso(request);

        assertThat(result.getTipoMovimiento()).isEqualTo("ingreso");
        assertThat(result.getCantidad()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(result.getInsumoId()).isEqualTo(insumoId);
    }

    @Test
    @DisplayName("registrarIngreso lanza ResourceNotFoundException cuando insumo no existe")
    void shouldThrowNotFound_whenInsumoDoesNotExistForIngreso() {
        UUID insumoId = UUID.randomUUID();
        CreateMovimientoRequest request = new CreateMovimientoRequest(insumoId, BigDecimal.valueOf(5), "Compra");

        when(insumoRepository.findById(insumoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventarioService.registrarIngreso(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("registrarRetiro crea movimiento de retiro exitosamente")
    void shouldRegisterRetiro_whenStockIsSufficient() {
        UUID insumoId = UUID.randomUUID();
        Insumo insumo = createTestInsumo();
        insumo.setId(insumoId);
        CreateMovimientoRequest request = new CreateMovimientoRequest(insumoId, BigDecimal.valueOf(3), "Evento");

        when(insumoRepository.findById(insumoId)).thenReturn(Optional.of(insumo));
        when(movimientoRepository.saveAndFlush(any(InsumoMovimiento.class))).thenAnswer(inv -> {
            InsumoMovimiento m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        InsumoMovimiento result = inventarioService.registrarRetiro(request);

        assertThat(result.getTipoMovimiento()).isEqualTo("retiro");
        assertThat(result.getCantidad()).isEqualByComparingTo(BigDecimal.valueOf(3));
    }

    @Test
    @DisplayName("registrarRetiro lanza InsufficientStockException cuando stock es insuficiente")
    void shouldThrowInsufficientStock_whenStockIsNotEnough() {
        UUID insumoId = UUID.randomUUID();
        Insumo insumo = createTestInsumo();
        insumo.setId(insumoId);
        insumo.setStockActual(BigDecimal.valueOf(2));
        CreateMovimientoRequest request = new CreateMovimientoRequest(insumoId, BigDecimal.valueOf(10), "Evento");

        when(insumoRepository.findById(insumoId)).thenReturn(Optional.of(insumo));
        DataIntegrityViolationException dbException = new DataIntegrityViolationException("constraint violation") {
            @Override
            public Throwable getMostSpecificCause() {
                return new RuntimeException("stock check constraint violated");
            }
        };
        when(movimientoRepository.saveAndFlush(any(InsumoMovimiento.class))).thenThrow(dbException);

        assertThatThrownBy(() -> inventarioService.registrarRetiro(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Cables XLR");
    }

    @Test
    @DisplayName("registrarRetiro relanza DataIntegrityViolationException cuando no es de stock")
    void shouldRethrowException_whenNotStockRelated() {
        UUID insumoId = UUID.randomUUID();
        Insumo insumo = createTestInsumo();
        insumo.setId(insumoId);
        CreateMovimientoRequest request = new CreateMovimientoRequest(insumoId, BigDecimal.valueOf(1), "Test");

        when(insumoRepository.findById(insumoId)).thenReturn(Optional.of(insumo));
        DataIntegrityViolationException dbException = new DataIntegrityViolationException("other constraint") {
            @Override
            public Throwable getMostSpecificCause() {
                return new RuntimeException("foreign key constraint violated");
            }
        };
        when(movimientoRepository.saveAndFlush(any(InsumoMovimiento.class))).thenThrow(dbException);

        assertThatThrownBy(() -> inventarioService.registrarRetiro(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("getMovimientos filtra por tipo cuando se proporciona")
    void shouldFilterByTipo_whenProvided() {
        UUID insumoId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<InsumoMovimiento> expected = new PageImpl<>(List.of());

        when(movimientoRepository.findByInsumoIdAndTipoMovimientoOrderByFechaDesc(insumoId, "ingreso", pageable))
                .thenReturn(expected);

        Page<InsumoMovimiento> result = inventarioService.getMovimientos(insumoId, "ingreso", pageable);

        verify(movimientoRepository).findByInsumoIdAndTipoMovimientoOrderByFechaDesc(insumoId, "ingreso", pageable);
    }

    @Test
    @DisplayName("getMovimientos retorna todos cuando tipo es null")
    void shouldReturnAll_whenTipoIsNull() {
        UUID insumoId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Page<InsumoMovimiento> expected = new PageImpl<>(List.of());

        when(movimientoRepository.findByInsumoIdOrderByFechaDesc(insumoId, pageable)).thenReturn(expected);

        inventarioService.getMovimientos(insumoId, null, pageable);

        verify(movimientoRepository).findByInsumoIdOrderByFechaDesc(insumoId, pageable);
    }
}
