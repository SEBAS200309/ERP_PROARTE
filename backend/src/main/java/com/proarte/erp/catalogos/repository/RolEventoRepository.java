package com.proarte.erp.catalogos.repository;

import com.proarte.erp.catalogos.entity.RolEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RolEventoRepository extends JpaRepository<RolEvento, UUID> {
}
