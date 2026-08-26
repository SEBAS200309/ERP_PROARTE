package com.proarte.erp.servicios.repository;

import com.proarte.erp.servicios.entity.ServicioDescuentoRecargo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServicioDescuentoRecargoRepository extends JpaRepository<ServicioDescuentoRecargo, UUID> {

    boolean existsByServicioIdAndDescuentoRecargoId(UUID servicioId, UUID descuentoRecargoId);
}
