package com.proarte.erp.presentaciones.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.presentaciones.dto.CreatePresentacionRequest;
import com.proarte.erp.presentaciones.dto.UpdatePresentacionRequest;
import com.proarte.erp.presentaciones.entity.Presentacion;
import com.proarte.erp.presentaciones.repository.PresentacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresentacionService {

    private final PresentacionRepository presentacionRepository;

    @Transactional(readOnly = true)
    public Page<Presentacion> getAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return presentacionRepository.searchByNombre(search, pageable);
        }
        return presentacionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Presentacion getById(UUID id) {
        return presentacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presentacion", "id", id));
    }

    @Transactional
    public Presentacion create(CreatePresentacionRequest request) {
        Presentacion presentacion = Presentacion.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .servicioId(request.servicioId())
                .build();
        presentacion.setActivo(true);

        Presentacion saved = presentacionRepository.save(presentacion);
        log.info("Presentacion created: id={}, nombre={}", saved.getId(), saved.getNombre());
        return saved;
    }

    @Transactional
    public Presentacion update(UUID id, UpdatePresentacionRequest request) {
        Presentacion presentacion = presentacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presentacion", "id", id));

        if (request.nombre() != null) {
            presentacion.setNombre(request.nombre());
        }
        if (request.descripcion() != null) {
            presentacion.setDescripcion(request.descripcion());
        }
        if (request.servicioId() != null) {
            presentacion.setServicioId(request.servicioId());
        }

        Presentacion updated = presentacionRepository.save(presentacion);
        log.info("Presentacion updated: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void delete(UUID id) {
        if (!presentacionRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("Presentacion", "id", id);
        }
        presentacionRepository.softDelete(id);
        log.info("Presentacion soft-deleted: id={}", id);
    }
}
