package com.proarte.erp.servicios.repository;

import com.proarte.erp.servicios.entity.TipoDescuentoRecargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TipoDescuentoRecargoRepository extends JpaRepository<TipoDescuentoRecargo, UUID> {
}
