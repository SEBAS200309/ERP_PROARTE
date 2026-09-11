package com.proarte.erp.servicios.repository;

import com.proarte.erp.servicios.entity.TipoDescuentoRecargo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TipoDescuentoRecargoRepository extends JpaRepository<TipoDescuentoRecargo, UUID> {
}
