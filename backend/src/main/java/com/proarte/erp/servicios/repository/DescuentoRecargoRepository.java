package com.proarte.erp.servicios.repository;

import com.proarte.erp.servicios.entity.DescuentoRecargo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DescuentoRecargoRepository extends JpaRepository<DescuentoRecargo, UUID>, JpaSpecificationExecutor<DescuentoRecargo> {

    Page<DescuentoRecargo> findByTipoId(UUID tipoId, Pageable pageable);

    @Modifying
    @Query("UPDATE DescuentoRecargo d SET d.activo = false, d.updatedAt = CURRENT_TIMESTAMP WHERE d.id = :id AND d.activo = true")
    void softDelete(@Param("id") UUID id);

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DescuentoRecargo d WHERE d.id = :id AND d.activo = true")
    boolean existsActiveById(@Param("id") UUID id);
}
