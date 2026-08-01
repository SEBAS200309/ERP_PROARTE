package com.proarte.erp.eventos.repository;

import com.proarte.erp.eventos.entity.EventoContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventoContactoRepository extends JpaRepository<EventoContacto, UUID> {

    List<EventoContacto> findByEventoId(UUID eventoId);

    void deleteByEventoIdAndId(UUID eventoId, UUID id);
}
