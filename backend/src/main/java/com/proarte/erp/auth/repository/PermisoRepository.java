package com.proarte.erp.auth.repository;

import com.proarte.erp.auth.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermisoRepository extends JpaRepository<Permiso, UUID> {

    List<Permiso> findByRolIdAndActivoTrue(UUID rolId);
}
