package com.proarte.erp.auth.repository;

import com.proarte.erp.auth.entity.Rol;
import com.proarte.erp.common.repository.SoftDeleteRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends SoftDeleteRepository<Rol> {
}
