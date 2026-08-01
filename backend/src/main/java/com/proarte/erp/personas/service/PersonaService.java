package com.proarte.erp.personas.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.personas.dto.AsociarEmpresaRequest;
import com.proarte.erp.personas.dto.CreatePersonaRequest;
import com.proarte.erp.personas.dto.UpdatePersonaRequest;
import com.proarte.erp.personas.entity.Persona;
import com.proarte.erp.personas.entity.PersonaEmpresa;
import com.proarte.erp.personas.repository.PersonaEmpresaRepository;
import com.proarte.erp.personas.repository.PersonaRepository;
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
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final PersonaEmpresaRepository personaEmpresaRepository;

    @Transactional(readOnly = true)
    public Page<Persona> getAll(String nombre, String documento, String email, Pageable pageable) {
        boolean hasNombre = nombre != null && !nombre.isBlank();
        boolean hasDocumento = documento != null && !documento.isBlank();
        boolean hasEmail = email != null && !email.isBlank();

        if (hasNombre) {
            return personaRepository.searchByNombre(nombre, pageable);
        } else if (hasDocumento) {
            return personaRepository.searchByDocumento(documento, pageable);
        } else if (hasEmail) {
            return personaRepository.searchByEmail(email, pageable);
        }

        return personaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Persona getById(UUID id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona", "id", id));
    }

    @Transactional
    public Persona create(CreatePersonaRequest request) {
        Persona persona = Persona.builder()
                .nombres(request.nombres())
                .apellidos(request.apellidos())
                .tipoDocumentoId(request.tipoDocumentoId())
                .documento(request.documento())
                .telefono(request.telefono())
                .email(request.email())
                .direccion(request.direccion())
                .rolEntidadId(request.rolEntidadId())
                .build();
        persona.setActivo(true);

        Persona saved = personaRepository.save(persona);
        log.info("Persona creada: id={}", saved.getId());
        return saved;
    }

    @Transactional
    public Persona update(UUID id, UpdatePersonaRequest request) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona", "id", id));

        if (request.nombres() != null) {
            persona.setNombres(request.nombres());
        }
        if (request.apellidos() != null) {
            persona.setApellidos(request.apellidos());
        }
        if (request.tipoDocumentoId() != null) {
            persona.setTipoDocumentoId(request.tipoDocumentoId());
        }
        if (request.documento() != null) {
            persona.setDocumento(request.documento());
        }
        if (request.telefono() != null) {
            persona.setTelefono(request.telefono());
        }
        if (request.email() != null) {
            persona.setEmail(request.email());
        }
        if (request.direccion() != null) {
            persona.setDireccion(request.direccion());
        }
        if (request.rolEntidadId() != null) {
            persona.setRolEntidadId(request.rolEntidadId());
        }

        Persona updated = personaRepository.save(persona);
        log.info("Persona actualizada: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void delete(UUID id) {
        if (!personaRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("Persona", "id", id);
        }
        personaRepository.softDelete(id);
        log.info("Persona eliminada (soft-delete): id={}", id);
    }

    @Transactional
    public PersonaEmpresa asociarEmpresa(UUID personaId, AsociarEmpresaRequest request) {
        if (!personaRepository.existsActiveById(personaId)) {
            throw new ResourceNotFoundException("Persona", "id", personaId);
        }

        PersonaEmpresa personaEmpresa = PersonaEmpresa.builder()
                .personaId(personaId)
                .empresaId(request.empresaId())
                .cargo(request.cargo())
                .build();

        PersonaEmpresa saved = personaEmpresaRepository.save(personaEmpresa);
        log.info("Persona asociada a empresa: personaId={}, empresaId={}", personaId, request.empresaId());
        return saved;
    }

    @Transactional
    public Persona asignarRol(UUID personaId, UUID rolEntidadId) {
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new ResourceNotFoundException("Persona", "id", personaId));

        persona.setRolEntidadId(rolEntidadId);
        Persona updated = personaRepository.save(persona);
        log.info("Rol asignado a persona: id={}, rolEntidadId={}", personaId, rolEntidadId);
        return updated;
    }
}
