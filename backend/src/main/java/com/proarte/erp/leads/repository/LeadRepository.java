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

@Repository
public interface LeadRepository extends SoftDeleteRepository<Lead> {

    Page<Lead> findByEstado(String estado, Pageable pageable);

    Page<Lead> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE LOWER(l.descripcion) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Lead> searchByDescripcion(@Param("search") String search, Pageable pageable);

    @Query("SELECT l FROM Lead l WHERE LOWER(l.descripcion) LIKE LOWER(CONCAT('%', :search, '%')) AND l.estado = :estado")
    Page<Lead> searchByDescripcionAndEstado(@Param("search") String search, @Param("estado") String estado, Pageable pageable);

    @Query("SELECT l.estado, COUNT(l) FROM Lead l GROUP BY l.estado")
    List<Object[]> countByEstado();
}
