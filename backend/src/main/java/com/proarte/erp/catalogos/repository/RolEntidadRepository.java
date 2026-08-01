package com.proarte.erp.catalogos.repository;

import com.proarte.erp.catalogos.entity.RolEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RolEntidadRepository extends JpaRepository<RolEntidad, UUID> {
}
