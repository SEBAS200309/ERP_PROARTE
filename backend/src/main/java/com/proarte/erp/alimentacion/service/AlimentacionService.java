package com.proarte.erp.alimentacion.service;

import com.proarte.erp.alimentacion.dto.CreateAlimentacionRequest;
import com.proarte.erp.alimentacion.entity.EventoAlimentacion;
import com.proarte.erp.alimentacion.repository.EventoAlimentacionRepository;
import com.proarte.erp.exception.InsufficientStockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlimentacionService {

    private final EventoAlimentacionRepository alimentacionRepository;

    @Transactional(readOnly = true)
    public Page<EventoAlimentacion> getByEvento(UUID eventoId, String tipo, Pageable pageable) {
        if (tipo != null && !tipo.isBlank()) {
            log.info("Fetching alimentacion for evento={} with tipo={}", eventoId, tipo);
            return alimentacionRepository.findByEventoIdAndTipoMovimientoOrderByFechaDesc(eventoId, tipo, pageable);
        }
        log.info("Fetching all alimentacion for evento={}", eventoId);
        return alimentacionRepository.findByEventoIdOrderByFechaDesc(eventoId, pageable);
    }

    @Transactional
    public EventoAlimentacion registrarIngreso(UUID eventoId, CreateAlimentacionRequest request) {
        EventoAlimentacion alimentacion = EventoAlimentacion.builder()
                .eventoId(eventoId)
                .descripcion(request.descripcion())
                .cantidad(request.cantidad())
                .tipoMovimiento("ingreso")
                .build();

        EventoAlimentacion saved = alimentacionRepository.save(alimentacion);
        log.info("Ingreso registered: id={}, eventoId={}, cantidad={}", saved.getId(), eventoId, request.cantidad());
        return saved;
    }

    @Transactional
    public EventoAlimentacion registrarRetiro(UUID eventoId, CreateAlimentacionRequest request) {
        try {
            EventoAlimentacion alimentacion = EventoAlimentacion.builder()
                    .eventoId(eventoId)
                    .descripcion(request.descripcion())
                    .cantidad(request.cantidad())
                    .tipoMovimiento("retiro")
                    .build();

            EventoAlimentacion saved = alimentacionRepository.save(alimentacion);
            alimentacionRepository.flush();
            log.info("Retiro registered: id={}, eventoId={}, cantidad={}", saved.getId(), eventoId, request.cantidad());
            return saved;
        } catch (DataIntegrityViolationException ex) {
            log.warn("Insufficient stock for retiro: eventoId={}, cantidad={}", eventoId, request.cantidad());
            throw new InsufficientStockException("No hay suficiente cantidad para este retiro");
        }
    }
}
