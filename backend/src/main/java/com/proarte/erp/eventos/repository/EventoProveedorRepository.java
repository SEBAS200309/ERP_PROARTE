package com.proarte.erp.eventos.repository;

import com.proarte.erp.eventos.entity.EventoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoProveedorRepository extends JpaRepository<EventoProveedor, UUID> {

    List<EventoProveedor> findByEventoId(UUID eventoId);

    void deleteByEventoIdAndId(UUID eventoId, UUID id);
}
