package com.proarte.erp.empresas.service;

import com.proarte.erp.empresas.dto.CreateEmpresaRequest;
import com.proarte.erp.empresas.dto.UpdateEmpresaRequest;
import com.proarte.erp.empresas.entity.Empresa;
import com.proarte.erp.empresas.repository.EmpresaRepository;
import com.proarte.erp.exception.ResourceNotFoundException;
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
public class EmpresaService {

    private static final String DEFAULT_ROL = "cliente";

    private final EmpresaRepository empresaRepository;

    @Transactional(readOnly = true)
    public Page<Empresa> getAll(String razonSocial, String nit, Pageable pageable) {
        boolean hasRazonSocial = razonSocial != null && !razonSocial.isBlank();
        boolean hasNit = nit != null && !nit.isBlank();

        if (hasRazonSocial) {
            return empresaRepository.searchByRazonSocial(razonSocial, pageable);
        } else if (hasNit) {
            return empresaRepository.searchByNit(nit, pageable);
        }

        return empresaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Empresa getById(UUID id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", id));
    }

    @Transactional
    public Empresa create(CreateEmpresaRequest request) {
        Empresa empresa = Empresa.builder()
                .razonSocial(request.razonSocial())
                .nit(request.nit())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .email(request.email())
                .rolEmpresa(request.rolEmpresa() != null && !request.rolEmpresa().isBlank()
                        ? request.rolEmpresa() : DEFAULT_ROL)
                .build();
        empresa.setActivo(true);

        Empresa saved = empresaRepository.save(empresa);
        log.info("Empresa creada: id={}", saved.getId());
        return saved;
    }

    @Transactional
    public Empresa update(UUID id, UpdateEmpresaRequest request) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", id));

        if (request.razonSocial() != null) {
            empresa.setRazonSocial(request.razonSocial());
        }
        if (request.nit() != null) {
            empresa.setNit(request.nit());
        }
        if (request.direccion() != null) {
            empresa.setDireccion(request.direccion());
        }
        if (request.telefono() != null) {
            empresa.setTelefono(request.telefono());
        }
        if (request.email() != null) {
            empresa.setEmail(request.email());
        }
        if (request.rolEmpresa() != null) {
            empresa.setRolEmpresa(request.rolEmpresa());
        }

        Empresa updated = empresaRepository.save(empresa);
        log.info("Empresa actualizada: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void delete(UUID id) {
        if (!empresaRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("Empresa", "id", id);
        }
        empresaRepository.softDelete(id);
        log.info("Empresa eliminada (soft-delete): id={}", id);
    }

    @Transactional
    public Empresa asignarRol(UUID empresaId, String rol) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa", "id", empresaId));

        empresa.setRolEmpresa(rol);
        Empresa updated = empresaRepository.save(empresa);
        log.info("Rol asignado a empresa: id={}, rol={}", empresaId, rol);
        return updated;
    }
}
