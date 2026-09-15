package com.proarte.erp.roles.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.roles.entity.Rol;

import java.util.Optional;

public interface RolRepository extends SoftDeleteRepository<Rol> {

    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);
}
