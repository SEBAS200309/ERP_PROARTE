package com.proarte.erp.eventos.repository;

import com.proarte.erp.eventos.entity.EventoObservacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoObservacionRepository extends JpaRepository<EventoObservacion, UUID> {

    List<EventoObservacion> findByEventoIdOrderByFechaDesc(UUID eventoId);
}
