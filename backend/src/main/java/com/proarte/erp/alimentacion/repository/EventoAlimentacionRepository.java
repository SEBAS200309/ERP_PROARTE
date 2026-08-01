package com.proarte.erp.alimentacion.repository;

import com.proarte.erp.alimentacion.entity.EventoAlimentacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventoAlimentacionRepository extends JpaRepository<EventoAlimentacion, UUID> {

    Page<EventoAlimentacion> findByEventoIdOrderByFechaDesc(UUID eventoId, Pageable pageable);

    Page<EventoAlimentacion> findByEventoIdAndTipoMovimientoOrderByFechaDesc(UUID eventoId, String tipoMovimiento, Pageable pageable);
}
