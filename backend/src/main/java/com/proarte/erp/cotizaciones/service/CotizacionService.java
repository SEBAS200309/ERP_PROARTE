package com.proarte.erp.cotizaciones.service;

import com.proarte.erp.cotizaciones.dto.*;
import com.proarte.erp.cotizaciones.entity.Cotizacion;
import com.proarte.erp.cotizaciones.entity.CotizacionItem;
import com.proarte.erp.cotizaciones.repository.CotizacionItemRepository;
import com.proarte.erp.cotizaciones.repository.CotizacionRepository;
import com.proarte.erp.exception.BusinessException;
import com.proarte.erp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final CotizacionItemRepository cotizacionItemRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public Page<CotizacionResponse> getAll(String search, UUID estadoId, UUID personaId, UUID empresaId,
            Pageable pageable) {
        Page<Cotizacion> pageCotizaciones;
        if (search != null && !search.isBlank()) {
            pageCotizaciones = cotizacionRepository.searchByCodigo(search, pageable);
        } else if (estadoId != null) {
            pageCotizaciones = cotizacionRepository.findByEstadoId(estadoId, pageable);
        } else if (personaId != null) {
            pageCotizaciones = cotizacionRepository.findByPersonaId(personaId, pageable);
        } else if (empresaId != null) {
            pageCotizaciones = cotizacionRepository.findByEmpresaId(empresaId, pageable);
        } else {
            pageCotizaciones = cotizacionRepository.findAll(pageable);
        }

        // El mapeo ocurre aquí adentro mientras la sesión de Hibernate sigue abierta
        return pageCotizaciones.map(CotizacionResponse::from);
    }

    @Transactional(readOnly = true)
    public CotizacionResponse getByIdDto(UUID id) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion", "id", id));
        return CotizacionResponse.from(cotizacion);
    }

    // Mantén tu método getById original por si lo usas internamente en otras
    // operaciones del backend
    @Transactional(readOnly = true)
    public Cotizacion getById(UUID id) {
        return cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion", "id", id));
    }

    @Transactional
    public Cotizacion create(CreateCotizacionRequest request) {
        String codigo = request.codigo();
        if (codigo == null || codigo.isBlank()) {
            codigo = generateCodigo();
        }

        Cotizacion cotizacion = Cotizacion.builder()
                .codigo(codigo)
                .estadoId(request.estadoId())
                .fechaVencimiento(request.fechaVencimiento())
                .personaId(request.personaId())
                .empresaId(request.empresaId())
                .total(BigDecimal.ZERO)
                .build();
        cotizacion.setActivo(true);

        Cotizacion saved = cotizacionRepository.save(cotizacion);
        log.info("Cotizacion creada: id={}, codigo={}", saved.getId(), saved.getCodigo());

        if (request.items() != null && !request.items().isEmpty()) {
            saveItems(saved.getId(), request.items());
            recalcularTotal(saved.getId());
            saved = cotizacionRepository.findById(saved.getId()).orElse(saved);
        }

        return saved;
    }

    @Transactional
    public Cotizacion update(UUID id, UpdateCotizacionRequest request) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion", "id", id));

        if (request.estadoId() != null) {
            cotizacion.setEstadoId(request.estadoId());
        }
        if (request.fechaVencimiento() != null) {
            cotizacion.setFechaVencimiento(request.fechaVencimiento());
        }
        if (request.personaId() != null) {
            cotizacion.setPersonaId(request.personaId());
        }
        if (request.empresaId() != null) {
            cotizacion.setEmpresaId(request.empresaId());
        }

        if (request.items() != null) {
            cotizacionItemRepository.deleteByCotizacionId(id);
            if (!request.items().isEmpty()) {
                saveItems(id, request.items());
            }
            recalcularTotal(id);
        }

        Cotizacion updated = cotizacionRepository.save(cotizacion);
        log.info("Cotizacion actualizada: id={}", updated.getId());
        return cotizacionRepository.findById(id).orElse(updated);
    }

    @Transactional
    public void delete(UUID id) {
        if (!cotizacionRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("Cotizacion", "id", id);
        }
        cotizacionRepository.softDelete(id);
        log.info("Cotizacion eliminada (soft-delete): id={}", id);
    }

    @Transactional
    public Cotizacion cambiarEstado(UUID id, CambiarEstadoRequest request) {
        Cotizacion cotizacion = cotizacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotizacion", "id", id));

        cotizacion.setEstadoId(request.estadoId());
        Cotizacion updated = cotizacionRepository.save(cotizacion);
        log.info("Estado de cotizacion cambiado: id={}, nuevoEstadoId={}", id, request.estadoId());
        return updated;
    }

    @Transactional(readOnly = true)
    public List<Cotizacion> getPorVencer(Integer dias) {
        if (dias == null || dias <= 0) {
            dias = 7;
        }
        return cotizacionRepository.findPorVencer(dias);
    }

    @Transactional
    public BigDecimal recalcularTotal(UUID cotizacionId) {
        BigDecimal total = jdbcTemplate.queryForObject(
                "SELECT fn_recalcular_total_cotizacion(?)",
                BigDecimal.class,
                cotizacionId);
        log.info("Total recalculado para cotizacion {}: {}", cotizacionId, total);
        return total;
    }

    private void saveItems(UUID cotizacionId, List<CotizacionItemRequest> items) {
        List<CotizacionItem> entities = items.stream()
                .map(item -> CotizacionItem.builder()
                        .cotizacionId(cotizacionId)
                        .servicioId(item.servicioId())
                        .cantidad(item.cantidad())
                        .precioUnitario(item.precioUnitario())
                        .descuentoRecargoId(item.descuentoRecargoId())
                        .build())
                .toList();
        cotizacionItemRepository.saveAll(entities);
    }

    private String generateCodigo() {
        int year = Year.now().getValue();
        String prefix = "COT-" + year + "-%";
        Integer maxSeq = cotizacionRepository.findMaxCodigoSequence(prefix);
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;
        return String.format("COT-%d-%03d", year, nextSeq);
    }

    @Transactional(readOnly = true)
    public long getTotalCotizacionesPendientes() {
        return cotizacionRepository.countCotizacionesPendientes();
    }
}