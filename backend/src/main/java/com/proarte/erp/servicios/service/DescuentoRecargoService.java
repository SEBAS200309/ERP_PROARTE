package com.proarte.erp.servicios.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.servicios.dto.AplicarDescuentoRecargoRequest;
import com.proarte.erp.servicios.dto.CreateDescuentoRecargoRequest;
import com.proarte.erp.servicios.dto.UpdateDescuentoRecargoRequest;
import com.proarte.erp.servicios.entity.DescuentoRecargo;
import com.proarte.erp.servicios.entity.EmpresaDescuentoRecargo;
import com.proarte.erp.servicios.entity.PersonaDescuentoRecargo;
import com.proarte.erp.servicios.entity.ServicioDescuentoRecargo;
import com.proarte.erp.servicios.repository.*;
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
public class DescuentoRecargoService {

    private final DescuentoRecargoRepository descuentoRecargoRepository;
    private final ServicioDescuentoRecargoRepository servicioDescuentoRecargoRepository;
    private final PersonaDescuentoRecargoRepository personaDescuentoRecargoRepository;
    private final EmpresaDescuentoRecargoRepository empresaDescuentoRecargoRepository;

    @Transactional(readOnly = true)
    public Page<DescuentoRecargo> getAllDescuentosRecargos(UUID tipoId, Pageable pageable) {
        if (tipoId != null) {
            return descuentoRecargoRepository.findByTipoId(tipoId, pageable);
        }
        return descuentoRecargoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public DescuentoRecargo getDescuentoRecargoById(UUID id) {
        return descuentoRecargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DescuentoRecargo", "id", id));
    }

    @Transactional
    public DescuentoRecargo createDescuentoRecargo(CreateDescuentoRecargoRequest request) {
        DescuentoRecargo descuentoRecargo = DescuentoRecargo.builder()
                .nombre(request.nombre())
                .valor(request.valor())
                .tipoId(request.tipoId())
                .activo(true)
                .build();

        DescuentoRecargo saved = descuentoRecargoRepository.save(descuentoRecargo);
        log.info("DescuentoRecargo creado: id={}, nombre={}", saved.getId(), saved.getNombre());
        return saved;
    }

    @Transactional
    public DescuentoRecargo updateDescuentoRecargo(UUID id, UpdateDescuentoRecargoRequest request) {
        DescuentoRecargo descuentoRecargo = descuentoRecargoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DescuentoRecargo", "id", id));

        if (request.nombre() != null) {
            descuentoRecargo.setNombre(request.nombre());
        }
        if (request.valor() != null) {
            descuentoRecargo.setValor(request.valor());
        }
        if (request.tipoId() != null) {
            descuentoRecargo.setTipoId(request.tipoId());
        }

        DescuentoRecargo updated = descuentoRecargoRepository.save(descuentoRecargo);
        log.info("DescuentoRecargo actualizado: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteDescuentoRecargo(UUID id) {
        if (!descuentoRecargoRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("DescuentoRecargo", "id", id);
        }
        descuentoRecargoRepository.softDelete(id);
        log.info("DescuentoRecargo eliminado (soft-delete): id={}", id);
    }

    @Transactional
    public void aplicar(AplicarDescuentoRecargoRequest request) {
        UUID descuentoRecargoId = request.descuentoRecargoId();

        if (!descuentoRecargoRepository.existsActiveById(descuentoRecargoId)) {
            throw new ResourceNotFoundException("DescuentoRecargo", "id", descuentoRecargoId);
        }

        if (request.servicioId() != null) {
            aplicarAServicio(request.servicioId(), descuentoRecargoId);
        }
        if (request.personaId() != null) {
            aplicarAPersona(request.personaId(), descuentoRecargoId);
        }
        if (request.empresaId() != null) {
            aplicarAEmpresa(request.empresaId(), descuentoRecargoId);
        }
    }

    private void aplicarAServicio(UUID servicioId, UUID descuentoRecargoId) {
        if (servicioDescuentoRecargoRepository.existsByServicioIdAndDescuentoRecargoId(servicioId, descuentoRecargoId)) {
            log.info("Descuento/recargo ya aplicado al servicio: servicioId={}, descuentoRecargoId={}", servicioId, descuentoRecargoId);
            return;
        }

        ServicioDescuentoRecargo junction = ServicioDescuentoRecargo.builder()
                .servicioId(servicioId)
                .descuentoRecargoId(descuentoRecargoId)
                .build();

        servicioDescuentoRecargoRepository.save(junction);
        log.info("Descuento/recargo aplicado a servicio: servicioId={}, descuentoRecargoId={}", servicioId, descuentoRecargoId);
    }

    private void aplicarAPersona(UUID personaId, UUID descuentoRecargoId) {
        if (personaDescuentoRecargoRepository.existsByPersonaIdAndDescuentoRecargoId(personaId, descuentoRecargoId)) {
            log.info("Descuento/recargo ya aplicado a persona: personaId={}, descuentoRecargoId={}", personaId, descuentoRecargoId);
            return;
        }

        PersonaDescuentoRecargo junction = PersonaDescuentoRecargo.builder()
                .personaId(personaId)
                .descuentoRecargoId(descuentoRecargoId)
                .build();

        personaDescuentoRecargoRepository.save(junction);
        log.info("Descuento/recargo aplicado a persona: personaId={}, descuentoRecargoId={}", personaId, descuentoRecargoId);
    }

    private void aplicarAEmpresa(UUID empresaId, UUID descuentoRecargoId) {
        if (empresaDescuentoRecargoRepository.existsByEmpresaIdAndDescuentoRecargoId(empresaId, descuentoRecargoId)) {
            log.info("Descuento/recargo ya aplicado a empresa: empresaId={}, descuentoRecargoId={}", empresaId, descuentoRecargoId);
            return;
        }

        EmpresaDescuentoRecargo junction = EmpresaDescuentoRecargo.builder()
                .empresaId(empresaId)
                .descuentoRecargoId(descuentoRecargoId)
                .build();

        empresaDescuentoRecargoRepository.save(junction);
        log.info("Descuento/recargo aplicado a empresa: empresaId={}, descuentoRecargoId={}", empresaId, descuentoRecargoId);
    }
}
