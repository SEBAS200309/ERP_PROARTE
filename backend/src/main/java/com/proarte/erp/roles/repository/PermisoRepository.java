package com.proarte.erp.roles.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.roles.entity.Permiso;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermisoRepository extends SoftDeleteRepository<Permiso> {

    Optional<Permiso> findByRolId(UUID rolId);

    List<Permiso> findAllByRolId(UUID rolId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Permiso p SET p.activo = false, p.updatedAt = CURRENT_TIMESTAMP WHERE p.rolId = :rolId AND p.activo = true")
    void softDeleteByRolId(@Param("rolId") UUID rolId);
}
