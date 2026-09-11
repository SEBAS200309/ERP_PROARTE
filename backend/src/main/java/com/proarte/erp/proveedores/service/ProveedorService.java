package com.proarte.erp.proveedores.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.proveedores.dto.*;
import com.proarte.erp.proveedores.entity.Portafolio;
import com.proarte.erp.proveedores.entity.Proveedor;
import com.proarte.erp.proveedores.entity.SolicitudServicio;
import com.proarte.erp.proveedores.repository.PortafolioRepository;
import com.proarte.erp.proveedores.repository.ProveedorRepository;
import com.proarte.erp.proveedores.repository.SolicitudServicioRepository;
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
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final PortafolioRepository portafolioRepository;
    private final SolicitudServicioRepository solicitudServicioRepository;

    // ===================== PROVEEDORES =====================

    @Transactional(readOnly = true)
    public Page<Proveedor> getAllProveedores(String search, String tipo, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            if ("persona".equalsIgnoreCase(tipo)) {
                return proveedorRepository.searchByEspecialidadAndPersonaNotNull(search, pageable);
            } else if ("empresa".equalsIgnoreCase(tipo)) {
                return proveedorRepository.searchByEspecialidadAndEmpresaNotNull(search, pageable);
            }
            return proveedorRepository.searchByEspecialidad(search, pageable);
        }
        if ("persona".equalsIgnoreCase(tipo)) {
            return proveedorRepository.findByPersonaIdIsNotNull(pageable);
        } else if ("empresa".equalsIgnoreCase(tipo)) {
            return proveedorRepository.findByEmpresaIdIsNotNull(pageable);
        }
        return proveedorRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Proveedor getProveedorById(UUID id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));
    }

    @Transactional
    public Proveedor createProveedor(CreateProveedorRequest request) {
        if (request.personaId() == null && request.empresaId() == null) {
            throw new com.proarte.erp.exception.BusinessException(
                "El proveedor debe estar vinculado a una persona o a una empresa");
        }
        Proveedor proveedor = Proveedor.builder()
                .personaId(request.personaId())
                .empresaId(request.empresaId())
                .especialidad(request.especialidad())
                .build();
        proveedor.setActivo(true);

        Proveedor saved = proveedorRepository.save(proveedor);
        log.info("Proveedor creado: id={}", saved.getId());
        return saved;
    }

    @Transactional
    public Proveedor updateProveedor(UUID id, UpdateProveedorRequest request) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));

        if (request.personaId() != null) {
            proveedor.setPersonaId(request.personaId());
        }
        if (request.empresaId() != null) {
            proveedor.setEmpresaId(request.empresaId());
        }
        if (request.especialidad() != null) {
            proveedor.setEspecialidad(request.especialidad());
        }

        Proveedor updated = proveedorRepository.save(proveedor);
        log.info("Proveedor actualizado: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteProveedor(UUID id) {
        if (!proveedorRepository.existsActiveById(id)) {
            throw new ResourceNotFoundException("Proveedor", "id", id);
        }
        proveedorRepository.softDelete(id);
        log.info("Proveedor eliminado (soft-delete): id={}", id);
    }

    // ===================== PORTAFOLIO =====================

    @Transactional(readOnly = true)
    public List<Portafolio> getPortafolioByProveedor(UUID proveedorId) {
        if (!proveedorRepository.existsActiveById(proveedorId)) {
            throw new ResourceNotFoundException("Proveedor", "id", proveedorId);
        }
        return portafolioRepository.findByProveedorId(proveedorId);
    }

    @Transactional
    public Portafolio createPortafolio(UUID proveedorId, CreatePortafolioRequest request) {
        if (!proveedorRepository.existsActiveById(proveedorId)) {
            throw new ResourceNotFoundException("Proveedor", "id", proveedorId);
        }

        Portafolio portafolio = Portafolio.builder()
                .proveedorId(proveedorId)
                .servicioId(request.servicioId())
                .precioUnitario(request.precioUnitario())
                .activo(true)
                .build();

        Portafolio saved = portafolioRepository.save(portafolio);
        log.info("Portafolio creado: id={}, proveedorId={}", saved.getId(), proveedorId);
        return saved;
    }

    @Transactional
    public Portafolio updatePortafolio(UUID portafolioId, UpdatePortafolioRequest request) {
        Portafolio portafolio = portafolioRepository.findById(portafolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portafolio", "id", portafolioId));

        if (request.servicioId() != null) {
            portafolio.setServicioId(request.servicioId());
        }
        if (request.precioUnitario() != null) {
            portafolio.setPrecioUnitario(request.precioUnitario());
        }

        Portafolio updated = portafolioRepository.save(portafolio);
        log.info("Portafolio actualizado: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void deletePortafolio(UUID portafolioId) {
        if (!portafolioRepository.existsActiveById(portafolioId)) {
            throw new ResourceNotFoundException("Portafolio", "id", portafolioId);
        }
        portafolioRepository.softDelete(portafolioId);
        log.info("Portafolio eliminado (soft-delete): id={}", portafolioId);
    }

    // ===================== SOLICITUDES =====================

    @Transactional(readOnly = true)
    public Page<SolicitudServicio> getAllSolicitudes(UUID estadoId, Pageable pageable) {
        if (estadoId != null) {
            return solicitudServicioRepository.findByEstadoId(estadoId, pageable);
        }
        return solicitudServicioRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<SolicitudServicio> getSolicitudesByProveedor(UUID proveedorId, Pageable pageable) {
        if (!proveedorRepository.existsActiveById(proveedorId)) {
            throw new ResourceNotFoundException("Proveedor", "id", proveedorId);
        }
        return solicitudServicioRepository.findByProveedorId(proveedorId, pageable);
    }

    @Transactional
    public SolicitudServicio createSolicitud(CreateSolicitudRequest request) {
        if (!proveedorRepository.existsActiveById(request.proveedorId())) {
            throw new ResourceNotFoundException("Proveedor", "id", request.proveedorId());
        }

        SolicitudServicio solicitud = SolicitudServicio.builder()
                .proveedorId(request.proveedorId())
                .servicioId(request.servicioId())
                .eventoId(request.eventoId())
                .estadoId(request.estadoId())
                .descripcion(request.descripcion())
                .build();
        solicitud.setActivo(true);

        SolicitudServicio saved = solicitudServicioRepository.save(solicitud);
        log.info("Solicitud de servicio creada: id={}, proveedorId={}", saved.getId(), request.proveedorId());
        return saved;
    }

    @Transactional
    public SolicitudServicio updateSolicitud(UUID solicitudId, UpdateSolicitudRequest request) {
        SolicitudServicio solicitud = solicitudServicioRepository.findById(solicitudId)
                .orElseThrow(() -> new ResourceNotFoundException("SolicitudServicio", "id", solicitudId));

        if (request.proveedorId() != null) {
            solicitud.setProveedorId(request.proveedorId());
        }
        if (request.servicioId() != null) {
            solicitud.setServicioId(request.servicioId());
        }
        if (request.eventoId() != null) {
            solicitud.setEventoId(request.eventoId());
        }
        if (request.estadoId() != null) {
            solicitud.setEstadoId(request.estadoId());
        }
        if (request.descripcion() != null) {
            solicitud.setDescripcion(request.descripcion());
        }

        SolicitudServicio updated = solicitudServicioRepository.save(solicitud);
        log.info("Solicitud de servicio actualizada: id={}", updated.getId());
        return updated;
    }

    @Transactional
    public void deleteSolicitud(UUID solicitudId) {
        if (!solicitudServicioRepository.existsActiveById(solicitudId)) {
            throw new ResourceNotFoundException("SolicitudServicio", "id", solicitudId);
        }
        solicitudServicioRepository.softDelete(solicitudId);
        log.info("Solicitud de servicio eliminada (soft-delete): id={}", solicitudId);
    }
}
