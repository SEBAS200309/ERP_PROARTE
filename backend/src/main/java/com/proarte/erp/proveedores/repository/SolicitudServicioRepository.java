package com.proarte.erp.proveedores.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.proveedores.entity.SolicitudServicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SolicitudServicioRepository extends SoftDeleteRepository<SolicitudServicio> {

    Page<SolicitudServicio> findByProveedorId(UUID proveedorId, Pageable pageable);

    Page<SolicitudServicio> findByEstadoId(UUID estadoId, Pageable pageable);

    @Query("SELECT s FROM SolicitudServicio s WHERE s.proveedorId = :proveedorId AND s.estadoId = :estadoId")
    Page<SolicitudServicio> findByProveedorIdAndEstadoId(
            @Param("proveedorId") UUID proveedorId,
            @Param("estadoId") UUID estadoId,
            Pageable pageable);
}
