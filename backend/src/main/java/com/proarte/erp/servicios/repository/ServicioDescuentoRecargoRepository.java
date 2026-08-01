package com.proarte.erp.servicios.repository;

import com.proarte.erp.servicios.entity.ServicioDescuentoRecargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServicioDescuentoRecargoRepository extends JpaRepository<ServicioDescuentoRecargo, UUID> {

    boolean existsByServicioIdAndDescuentoRecargoId(UUID servicioId, UUID descuentoRecargoId);
}
