package com.proarte.erp.inventario.service;

import com.proarte.erp.exception.InsufficientStockException;
import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.inventario.dto.CreateInsumoRequest;
import com.proarte.erp.inventario.dto.CreateMovimientoRequest;
import com.proarte.erp.inventario.entity.Insumo;
import com.proarte.erp.inventario.entity.InsumoMovimiento;
import com.proarte.erp.inventario.repository.InsumoMovimientoRepository;
import com.proarte.erp.inventario.repository.InsumoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InsumoRepository insumoRepository;
    private final InsumoMovimientoRepository movimientoRepository;

    @Transactional(readOnly = true)
    public Page<Insumo> getAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return insumoRepository.searchByNombre(search, pageable);
        }
        return insumoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Insumo getById(UUID id) {
        return insumoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo", "id", id));
    }

    @Transactional
    public Insumo createInsumo(CreateInsumoRequest request) {
        Insumo insumo = Insumo.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .unidadMedidaId(request.unidadMedidaId())
                .stockActual(BigDecimal.ZERO)
                .build();
        insumo.setActivo(true);

        Insumo saved = insumoRepository.save(insumo);
        log.info("Insumo created: id={}, nombre={}", saved.getId(), saved.getNombre());
        return saved;
    }

    @Transactional
    public InsumoMovimiento registrarIngreso(CreateMovimientoRequest request) {
        // Verify the insumo exists
        Insumo insumo = insumoRepository.findById(request.insumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo", "id", request.insumoId()));

        InsumoMovimiento movimiento = InsumoMovimiento.builder()
                .insumoId(request.insumoId())
                .tipoMovimiento("ingreso")
                .cantidad(request.cantidad())
                .fecha(OffsetDateTime.now())
                .motivo(request.motivo())
                .build();

        InsumoMovimiento saved = movimientoRepository.save(movimiento);
        log.info("Ingreso registered: insumoId={}, cantidad={}", request.insumoId(), request.cantidad());
        return saved;
    }

    @Transactional
    public InsumoMovimiento registrarRetiro(CreateMovimientoRequest request) {
        // Verify the insumo exists
        Insumo insumo = insumoRepository.findById(request.insumoId())
                .orElseThrow(() -> new ResourceNotFoundException("Insumo", "id", request.insumoId()));

        InsumoMovimiento movimiento = InsumoMovimiento.builder()
                .insumoId(request.insumoId())
                .tipoMovimiento("retiro")
                .cantidad(request.cantidad())
                .fecha(OffsetDateTime.now())
                .motivo(request.motivo())
                .build();

        try {
            InsumoMovimiento saved = movimientoRepository.saveAndFlush(movimiento);
            log.info("Retiro registered: insumoId={}, cantidad={}", request.insumoId(), request.cantidad());
            return saved;
        } catch (DataIntegrityViolationException ex) {
            String message = ex.getMostSpecificCause().getMessage();
            if (message != null && message.contains("stock")) {
                throw new InsufficientStockException(
                        insumo.getNombre(), request.cantidad(), insumo.getStockActual());
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public Page<InsumoMovimiento> getMovimientos(UUID insumoId, String tipo, Pageable pageable) {
        if (tipo != null && !tipo.isBlank()) {
            return movimientoRepository.findByInsumoIdAndTipoMovimientoOrderByFechaDesc(insumoId, tipo, pageable);
        }
        return movimientoRepository.findByInsumoIdOrderByFechaDesc(insumoId, pageable);
    }
}
