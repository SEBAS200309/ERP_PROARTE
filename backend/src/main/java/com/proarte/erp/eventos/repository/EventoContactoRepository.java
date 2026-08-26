package com.proarte.erp.eventos.repository;

import com.proarte.erp.eventos.entity.EventoContacto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoContactoRepository extends JpaRepository<EventoContacto, UUID> {

    List<EventoContacto> findByEventoId(UUID eventoId);

    void deleteByEventoIdAndId(UUID eventoId, UUID id);
}
