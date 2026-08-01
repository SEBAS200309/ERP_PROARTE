package com.proarte.erp.catalogos.repository;

import com.proarte.erp.catalogos.entity.CategoriaServicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoriaServicioRepository extends JpaRepository<CategoriaServicio, UUID> {
}
