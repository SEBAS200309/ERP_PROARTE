package com.proarte.erp.servicios.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.servicios.dto.CreateServicioRequest;
import com.proarte.erp.servicios.dto.UpdateServicioRequest;
import com.proarte.erp.servicios.entity.Servicio;
import com.proarte.erp.servicios.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioService {

    private final ServicioRepository servicioRepository;

    @Transactional(readOnly = true)
    public Page<Servicio> getAllServicios(String search, UUID categoriaId, Pageable pageable) {
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasCategoria = categoriaId != null;

        if (hasSearch && hasCategoria) {
            return servicioRepository.searchByNombreAndCategoriaId(search, categoriaId, pageable);
        }
        if (hasSearch) {
            return servicioRepository.searchByNombre(search, pageable);
        }
        if (hasCategoria) {
            return servicioRepository.findByCategoriaId(categoriaId, pageable);
        }
        return servicioRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Servicio getServicioById(UUID id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio", "id", id));
    }

    @Transactional
    public Servicio createServicio(CreateServicioRequest request) {
        Servicio servicio = Servicio.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .esPropio(request.esPropio() != null ? request.esPropio() : true)
                .requiereOc(request.requiereOc() != null ? request.requiereOc() : false)
                .servicioPadreId(request.servicioPadreId())
                .categoriaId(request.categoriaId())
                .activo(true)
                .build();

        Servicio saved = servicioRepository.save(servicio);
        log.info("Servicio creado: id={}, nombre={}", saved.getId(), saved.getNombre());
        return saved;
    }

    @Transactional
    public Servicio updateServicio(UUID id, UpdateServicioRequest request) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio", "id", id));

        if (request.nombre() != null) {
            servicio.setNombre(request.nombre());
        }
        if (request.descripcion() != null) {
            servicio.setDescripcion(request.descripcion());
        }
        if (request.esPropio() != null) {
            servicio.setEsPropio(request.esPropio());
        }
        if (request.requiereOc() != null) {
            servicio.setRequiereOc(request.requiereOc());
        }
        if (request.servicioPadreId() != null) {
            servicio.setServicioPadreId(request.servicioPadreId());
        }
        if (request.categoriaId() != null) {
            servicio.setCategoriaId(request.categoriaId());
        }

        Servicio updated = servicioRepository.save(servicio);
        log.info("Servicio actualizado: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteServicio(UUID id) {
        if (!servicioRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("Servicio", "id", id);
        }
        servicioRepository.softDelete(id);
        log.info("Servicio eliminado (soft-delete): id={}", id);
    }

    @Transactional(readOnly = true)
    public List<Servicio> getSubservicios(UUID servicioId) {
        if (!servicioRepository.existsActiveById(servicioId)) {
            throw new ResourceNotFoundException("Servicio", "id", servicioId);
        }
        return servicioRepository.findByServicioPadreId(servicioId);
    }

    @Transactional
    public Servicio categorizar(UUID servicioId, UUID categoriaId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio", "id", servicioId));

        servicio.setCategoriaId(categoriaId);
        Servicio updated = servicioRepository.save(servicio);
        log.info("Servicio categorizado: id={}, categoriaId={}", servicioId, categoriaId);
        return updated;
    }
}
