package com.proarte.erp.catalogos.repository;

import com.proarte.erp.catalogos.entity.UnidadMedida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, UUID> {
}
