package com.proarte.erp.mensajes.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.mensajes.dto.CreateMensajeRequest;
import com.proarte.erp.mensajes.dto.UpdateMensajeRequest;
import com.proarte.erp.mensajes.entity.Mensaje;
import com.proarte.erp.mensajes.repository.MensajeRepository;
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
public class MensajeService {

    private final MensajeRepository mensajeRepository;

    @Transactional(readOnly = true)
    public Page<Mensaje> getAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return mensajeRepository.searchByNombre(search, pageable);
        }
        return mensajeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Mensaje getById(UUID id) {
        return mensajeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje", "id", id));
    }

    @Transactional
    public Mensaje create(CreateMensajeRequest request) {
        Mensaje mensaje = Mensaje.builder()
                .nombre(request.nombre())
                .contenido(request.contenido())
                .build();
        mensaje.setActivo(true);

        Mensaje saved = mensajeRepository.save(mensaje);
        log.info("Mensaje creado: id={}, nombre={}", saved.getId(), saved.getNombre());
        return saved;
    }

    @Transactional
    public Mensaje update(UUID id, UpdateMensajeRequest request) {
        Mensaje mensaje = mensajeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje", "id", id));

        if (request.nombre() != null) {
            mensaje.setNombre(request.nombre());
        }
        if (request.contenido() != null) {
            mensaje.setContenido(request.contenido());
        }

        Mensaje updated = mensajeRepository.save(mensaje);
        log.info("Mensaje actualizado: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void delete(UUID id) {
        if (!mensajeRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("Mensaje", "id", id);
        }
        mensajeRepository.softDelete(id);
        log.info("Mensaje eliminado (soft-delete): id={}", id);
    }
}
