package com.proarte.erp.auth.repository;

import com.proarte.erp.auth.entity.Permiso;
import com.proarte.erp.common.repository.SoftDeleteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermisoRepository extends SoftDeleteRepository<Permiso> {

    List<Permiso> findByRolId(UUID rolId);
}
