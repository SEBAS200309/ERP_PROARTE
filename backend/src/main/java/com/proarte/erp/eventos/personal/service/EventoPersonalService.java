package com.proarte.erp.eventos.personal.service;

import com.proarte.erp.eventos.personal.dto.CreateEventoPersonalRequest;
import com.proarte.erp.eventos.personal.dto.UpdateEventoPersonalRequest;
import com.proarte.erp.eventos.personal.entity.EventoPersonal;
import com.proarte.erp.eventos.personal.repository.EventoPersonalRepository;
import com.proarte.erp.eventos.repository.EventoRepository;
import com.proarte.erp.exception.BusinessException;
import com.proarte.erp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventoPersonalService {

    private final EventoPersonalRepository eventoPersonalRepository;
    private final EventoRepository eventoRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional(readOnly = true)
    public List<EventoPersonal> getByEventoId(UUID eventoId) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }
        return eventoPersonalRepository.findByEventoId(eventoId);
    }

    @Transactional
    public EventoPersonal create(UUID eventoId, CreateEventoPersonalRequest request) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }

        EventoPersonal personal = EventoPersonal.builder()
                .eventoId(eventoId)
                .personaId(request.personaId())
                .proveedorId(request.proveedorId())
                .servicioId(request.servicioId())
                .tieneArl(request.tieneArl() != null ? request.tieneArl() : false)
                .tieneOp(request.tieneOp() != null ? request.tieneOp() : false)
                .observaciones(request.observaciones())
                .valorTurno(BigDecimal.ZERO)
                .build();

        EventoPersonal saved = eventoPersonalRepository.save(personal);
        log.info("Personal agregado al evento: eventoId={}, personaId={}", eventoId, request.personaId());
        return saved;
    }

    @Transactional
    public EventoPersonal update(UUID eventoId, UUID personalId, UpdateEventoPersonalRequest request) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }

        EventoPersonal personal = eventoPersonalRepository.findById(personalId)
                .orElseThrow(() -> new ResourceNotFoundException("EventoPersonal", "id", personalId));

        if (request.servicioId() != null) {
            personal.setServicioId(request.servicioId());
        }
        if (request.tieneArl() != null) {
            personal.setTieneArl(request.tieneArl());
        }
        if (request.tieneOp() != null) {
            personal.setTieneOp(request.tieneOp());
        }
        if (request.observaciones() != null) {
            personal.setObservaciones(request.observaciones());
        }

        EventoPersonal updated = eventoPersonalRepository.save(personal);
        log.info("Personal actualizado: eventoId={}, personalId={}", eventoId, personalId);
        return updated;
    }

    @Transactional
    public void delete(UUID eventoId, UUID personalId) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }
        if (!eventoPersonalRepository.existsById(personalId)) {
            throw new ResourceNotFoundException("EventoPersonal", "id", personalId);
        }
        eventoPersonalRepository.deleteById(personalId);
        log.info("Personal removido del evento: eventoId={}, personalId={}", eventoId, personalId);
    }

    @Transactional
    public BigDecimal calcularValorTurno(UUID eventoPersonalId) {
        try {
            BigDecimal valor = jdbcTemplate.queryForObject(
                    "SELECT fn_calcular_valor_turno(?)",
                    BigDecimal.class,
                    eventoPersonalId
            );
            log.info("Valor turno calculado: eventoPersonalId={}, valor={}", eventoPersonalId, valor);
            return valor;
        } catch (DataAccessException ex) {
            String message = ex.getMostSpecificCause().getMessage();
            throw new BusinessException(message != null ? message : "Error al calcular el valor del turno");
        }
    }
}
