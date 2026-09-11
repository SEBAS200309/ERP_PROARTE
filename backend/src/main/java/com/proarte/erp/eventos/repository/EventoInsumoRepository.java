package com.proarte.erp.eventos.repository;

import com.proarte.erp.eventos.entity.EventoInsumo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoInsumoRepository extends JpaRepository<EventoInsumo, UUID> {

    List<EventoInsumo> findByEventoId(UUID eventoId);

    void deleteByEventoIdAndId(UUID eventoId, UUID id);
}
