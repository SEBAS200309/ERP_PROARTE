package com.proarte.erp.ordenes.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.ordenes.dto.CreateOrdenCompraRequest;
import com.proarte.erp.ordenes.dto.UpdateOrdenCompraRequest;
import com.proarte.erp.ordenes.entity.OrdenCompra;
import com.proarte.erp.ordenes.repository.OrdenCompraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;

    @Transactional(readOnly = true)
    public Page<OrdenCompra> getAll(String search, UUID estadoId, UUID solicitudId, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return ordenCompraRepository.searchByCodigo(search, pageable);
        }
        if (estadoId != null) {
            return ordenCompraRepository.findByEstadoId(estadoId, pageable);
        }
        if (solicitudId != null) {
            return ordenCompraRepository.findBySolicitudId(solicitudId, pageable);
        }
        return ordenCompraRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public OrdenCompra getById(UUID id) {
        return ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrdenCompra", "id", id));
    }

    @Transactional
    public OrdenCompra create(CreateOrdenCompraRequest request) {
        String codigo = request.codigo();
        if (codigo == null || codigo.isBlank()) {
            codigo = generateCodigo();
        }

        OrdenCompra orden = OrdenCompra.builder()
                .codigo(codigo)
                .solicitudId(request.solicitudId())
                .descripcion(request.descripcion())
                .monto(request.monto())
                .estadoId(request.estadoId())
                .build();
        orden.setActivo(true);

        OrdenCompra saved = ordenCompraRepository.save(orden);
        log.info("Orden de compra creada: id={}, codigo={}", saved.getId(), saved.getCodigo());
        return saved;
    }

    @Transactional
    public OrdenCompra update(UUID id, UpdateOrdenCompraRequest request) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrdenCompra", "id", id));

        if (request.solicitudId() != null) {
            orden.setSolicitudId(request.solicitudId());
        }
        if (request.descripcion() != null) {
            orden.setDescripcion(request.descripcion());
        }
        if (request.monto() != null) {
            orden.setMonto(request.monto());
        }
        if (request.estadoId() != null) {
            orden.setEstadoId(request.estadoId());
        }

        OrdenCompra updated = ordenCompraRepository.save(orden);
        log.info("Orden de compra actualizada: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void delete(UUID id) {
        if (!ordenCompraRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("OrdenCompra", "id", id);
        }
        ordenCompraRepository.softDelete(id);
        log.info("Orden de compra eliminada (soft-delete): id={}", id);
    }

    @Transactional(readOnly = true)
    public List<OrdenCompra> findByIds(List<UUID> ids) {
        return ordenCompraRepository.findAllByIdIn(ids);
    }

    private String generateCodigo() {
        int year = Year.now().getValue();
        String prefix = "OC-" + year + "-%";
        Integer maxSeq = ordenCompraRepository.findMaxCodigoSequence(prefix);
        int nextSeq = (maxSeq != null ? maxSeq : 0) + 1;
        return String.format("OC-%d-%03d", year, nextSeq);
    }
}
