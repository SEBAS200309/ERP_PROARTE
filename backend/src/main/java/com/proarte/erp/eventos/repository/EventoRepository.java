package com.proarte.erp.eventos.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.eventos.entity.Evento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface EventoRepository extends SoftDeleteRepository<Evento> {

    Page<Evento> findByEstadoId(UUID estadoId, Pageable pageable);

    @Query("SELECT e FROM Evento e WHERE LOWER(e.nombre) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Evento> searchByNombre(@Param("search") String search, Pageable pageable);

    @Query("SELECT e FROM Evento e WHERE e.estadoId = :estadoId AND LOWER(e.nombre) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Evento> searchByNombreAndEstadoId(@Param("search") String search, @Param("estadoId") UUID estadoId, Pageable pageable);
}
