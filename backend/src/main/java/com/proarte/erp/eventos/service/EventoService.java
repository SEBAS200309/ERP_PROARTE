package com.proarte.erp.eventos.service;

import com.proarte.erp.eventos.dto.*;
import com.proarte.erp.eventos.entity.*;
import com.proarte.erp.eventos.repository.*;
import com.proarte.erp.exception.BusinessException;
import com.proarte.erp.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final EventoContactoRepository eventoContactoRepository;
    private final EventoProveedorRepository eventoProveedorRepository;
    private final EventoObservacionRepository eventoObservacionRepository;
    private final EventoInsumoRepository eventoInsumoRepository;
    private final JdbcTemplate jdbcTemplate;

    // ===================== EVENTO CRUD =====================

    @Transactional(readOnly = true)
    public Page<Evento> getAll(String search, UUID estadoId, Pageable pageable) {
        if (search != null && !search.isBlank() && estadoId != null) {
            return eventoRepository.searchByNombreAndEstadoId(search, estadoId, pageable);
        }
        if (search != null && !search.isBlank()) {
            return eventoRepository.searchByNombre(search, pageable);
        }
        if (estadoId != null) {
            return eventoRepository.findByEstadoId(estadoId, pageable);
        }
        return eventoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Evento getById(UUID id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", id));
    }

    @Transactional
    public Evento create(CreateEventoRequest request) {
        Evento evento = Evento.builder()
                .cotizacionId(request.cotizacionId())
                .nombre(request.nombre())
                .fechaInicio(request.fechaInicio())
                .fechaFin(request.fechaFin())
                .lugar(request.lugar())
                .estadoId(request.estadoId())
                .build();
        evento.setActivo(true);

        Evento saved = eventoRepository.save(evento);
        log.info("Evento creado: id={}, nombre={}", saved.getId(), saved.getNombre());
        return saved;
    }

    @Transactional
    public Evento update(UUID id, UpdateEventoRequest request) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento", "id", id));

        if (request.nombre() != null) {
            evento.setNombre(request.nombre());
        }
        if (request.fechaInicio() != null) {
            evento.setFechaInicio(request.fechaInicio());
        }
        if (request.fechaFin() != null) {
            evento.setFechaFin(request.fechaFin());
        }
        if (request.lugar() != null) {
            evento.setLugar(request.lugar());
        }
        if (request.estadoId() != null) {
            evento.setEstadoId(request.estadoId());
        }

        Evento updated = eventoRepository.save(evento);
        log.info("Evento actualizado: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void delete(UUID id) {
        if (!eventoRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("Evento", "id", id);
        }
        eventoRepository.softDelete(id);
        log.info("Evento eliminado (soft-delete): id={}", id);
    }

    @Transactional
    public Evento crearDesdeCotizacion(UUID cotizacionId) {
        try {
            UUID eventoId = jdbcTemplate.queryForObject(
                    "SELECT fn_crear_evento_desde_cotizacion(?)",
                    UUID.class,
                    cotizacionId
            );
            log.info("Evento creado desde cotización: cotizacionId={}, eventoId={}", cotizacionId, eventoId);
            return eventoRepository.findById(eventoId)
                    .orElseThrow(() -> new BusinessException("Error al recuperar el evento creado"));
        } catch (org.springframework.dao.DataAccessException ex) {
            String message = ex.getMostSpecificCause().getMessage();
            throw new BusinessException(message != null ? message : "Error al crear evento desde cotización");
        }
    }

    // ===================== CONTACTOS =====================

    @Transactional
    public EventoContacto addContacto(UUID eventoId, EventoContactoRequest request) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }

        EventoContacto contacto = EventoContacto.builder()
                .eventoId(eventoId)
                .personaId(request.personaId())
                .rolEventoId(request.rolEventoId())
                .observaciones(request.observaciones())
                .build();

        EventoContacto saved = eventoContactoRepository.save(contacto);
        log.info("Contacto agregado al evento: eventoId={}, personaId={}", eventoId, request.personaId());
        return saved;
    }

    @Transactional
    public void removeContacto(UUID eventoId, UUID contactoId) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }
        if (!eventoContactoRepository.existsById(contactoId)) {
            throw new ResourceNotFoundException("EventoContacto", "id", contactoId);
        }
        eventoContactoRepository.deleteById(contactoId);
        log.info("Contacto removido del evento: eventoId={}, contactoId={}", eventoId, contactoId);
    }

    @Transactional(readOnly = true)
    public List<EventoContacto> getContactos(UUID eventoId) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }
        return eventoContactoRepository.findByEventoId(eventoId);
    }

    // ===================== PROVEEDORES =====================

    @Transactional
    public EventoProveedor addProveedor(UUID eventoId, EventoProveedorRequest request) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }

        EventoProveedor proveedor = EventoProveedor.builder()
                .eventoId(eventoId)
                .proveedorId(request.proveedorId())
                .servicioId(request.servicioId())
                .build();

        EventoProveedor saved = eventoProveedorRepository.save(proveedor);
        log.info("Proveedor agregado al evento: eventoId={}, proveedorId={}", eventoId, request.proveedorId());
        return saved;
    }

    @Transactional
    public void removeProveedor(UUID eventoId, UUID proveedorId) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }
        if (!eventoProveedorRepository.existsById(proveedorId)) {
            throw new ResourceNotFoundException("EventoProveedor", "id", proveedorId);
        }
        eventoProveedorRepository.deleteById(proveedorId);
        log.info("Proveedor removido del evento: eventoId={}, registroId={}", eventoId, proveedorId);
    }

    @Transactional(readOnly = true)
    public List<EventoProveedor> getProveedores(UUID eventoId) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }
        return eventoProveedorRepository.findByEventoId(eventoId);
    }

    // ===================== OBSERVACIONES =====================

    @Transactional
    public EventoObservacion addObservacion(UUID eventoId, ObservacionRequest request) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }

        EventoObservacion observacion = EventoObservacion.builder()
                .eventoId(eventoId)
                .texto(request.texto())
                .fecha(OffsetDateTime.now())
                .build();

        EventoObservacion saved = eventoObservacionRepository.save(observacion);
        log.info("Observación agregada al evento: eventoId={}", eventoId);
        return saved;
    }

    @Transactional
    public EventoObservacion updateObservacion(UUID observacionId, ObservacionRequest request) {
        EventoObservacion observacion = eventoObservacionRepository.findById(observacionId)
                .orElseThrow(() -> new ResourceNotFoundException("EventoObservacion", "id", observacionId));

        observacion.setTexto(request.texto());
        EventoObservacion updated = eventoObservacionRepository.save(observacion);
        log.info("Observación actualizada: id={}", observacionId);
        return updated;
    }

    @Transactional(readOnly = true)
    public List<EventoObservacion> getObservaciones(UUID eventoId) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }
        return eventoObservacionRepository.findByEventoIdOrderByFechaDesc(eventoId);
    }

    // ===================== INSUMOS =====================

    @Transactional
    public EventoInsumo addInsumo(UUID eventoId, EventoInsumoRequest request) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }

        EventoInsumo insumo = EventoInsumo.builder()
                .eventoId(eventoId)
                .insumoId(request.insumoId())
                .cantidad(request.cantidad())
                .build();

        EventoInsumo saved = eventoInsumoRepository.save(insumo);
        log.info("Insumo agregado al evento: eventoId={}, insumoId={}", eventoId, request.insumoId());
        return saved;
    }

    @Transactional
    public void removeInsumo(UUID eventoId, UUID insumoId) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }
        if (!eventoInsumoRepository.existsById(insumoId)) {
            throw new ResourceNotFoundException("EventoInsumo", "id", insumoId);
        }
        eventoInsumoRepository.deleteById(insumoId);
        log.info("Insumo removido del evento: eventoId={}, registroId={}", eventoId, insumoId);
    }

    @Transactional(readOnly = true)
    public List<EventoInsumo> getInsumos(UUID eventoId) {
        if (!eventoRepository.existsActiveById(eventoId)) {
            throw new ResourceNotFoundException("Evento", "id", eventoId);
        }
        return eventoInsumoRepository.findByEventoId(eventoId);
    }
}
