package com.proarte.erp.eventos.personal.repository;

import com.proarte.erp.eventos.personal.entity.EventoPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventoPersonalRepository extends JpaRepository<EventoPersonal, UUID> {

    List<EventoPersonal> findByEventoId(UUID eventoId);

    void deleteByEventoIdAndId(UUID eventoId, UUID id);
}
