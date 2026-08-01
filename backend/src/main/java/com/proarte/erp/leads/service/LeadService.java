package com.proarte.erp.leads.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.leads.dto.CreateLeadRequest;
import com.proarte.erp.leads.dto.UpdateLeadRequest;
import com.proarte.erp.leads.entity.Lead;
import com.proarte.erp.leads.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;

    @Transactional(readOnly = true)
    public Page<Lead> getAll(Pageable pageable) {
        return leadRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Lead> getAll(String search, UUID estadoId, Pageable pageable) {
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasEstadoId = estadoId != null;

        if (hasSearch && hasEstadoId) {
            return leadRepository.searchByDescripcionAndEstadoId(search, estadoId, pageable);
        } else if (hasSearch) {
            return leadRepository.searchByDescripcion(search, pageable);
        } else if (hasEstadoId) {
            return leadRepository.findByEstadoId(estadoId, pageable);
        }

        return leadRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Lead getById(UUID id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "id", id));
    }

    @Transactional
    public Lead create(CreateLeadRequest request) {
        Lead lead = Lead.builder()
                .descripcion(request.descripcion())
                .estadoId(request.estadoId())
                .personaId(request.personaId())
                .empresaId(request.empresaId())
                .build();
        lead.setActivo(true);

        Lead saved = leadRepository.save(lead);
        log.info("Lead creado: id={}", saved.getId());
        return saved;
    }

    @Transactional
    public Lead update(UUID id, UpdateLeadRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", "id", id));

        if (request.descripcion() != null) {
            lead.setDescripcion(request.descripcion());
        }
        if (request.estadoId() != null) {
            lead.setEstadoId(request.estadoId());
        }
        if (request.personaId() != null) {
            lead.setPersonaId(request.personaId());
        }
        if (request.empresaId() != null) {
            lead.setEmpresaId(request.empresaId());
        }

        Lead updated = leadRepository.save(lead);
        log.info("Lead actualizado: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void delete(UUID id) {
        if (!leadRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("Lead", "id", id);
        }
        leadRepository.softDelete(id);
        log.info("Lead eliminado (soft-delete): id={}", id);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getEstadisticas() {
        List<Object[]> results = leadRepository.countByEstado();
        Map<String, Long> estadisticas = new LinkedHashMap<>();

        for (Object[] row : results) {
            String estado = (String) row[0];
            Long count = (Long) row[1];
            estadisticas.put(estado, count);
        }

        return estadisticas;
    }
}
