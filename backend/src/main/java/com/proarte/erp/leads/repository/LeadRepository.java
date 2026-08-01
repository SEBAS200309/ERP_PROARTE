package com.proarte.erp.leads.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.leads.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeadRepository extends SoftDeleteRepository<Lead> {

    Page<Lead> findByEstadoId(UUID estadoId, Pageable pageable);

    Page<Lead> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE LOWER(l.descripcion) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Lead> searchByDescripcion(@Param("search") String search, Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE LOWER(l.descripcion) LIKE LOWER(CONCAT('%', :search, '%')) AND l.estadoId = :estadoId")
    Page<Lead> searchByDescripcionAndEstadoId(@Param("search") String search, @Param("estadoId") UUID estadoId, Pageable pageable);

    @Query(value = "SELECT e.nombre, COUNT(l.id) FROM lead l JOIN estado e ON e.id = l.estado_id WHERE l.activo = true GROUP BY e.nombre", nativeQuery = true)
    List<Object[]> countByEstado();
}
