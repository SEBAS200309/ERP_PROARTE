package com.proarte.erp.eventos.repository;

import com.proarte.erp.eventos.entity.EventoInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventoInsumoRepository extends JpaRepository<EventoInsumo, UUID> {

    List<EventoInsumo> findByEventoId(UUID eventoId);

    void deleteByEventoIdAndId(UUID eventoId, UUID id);
}
