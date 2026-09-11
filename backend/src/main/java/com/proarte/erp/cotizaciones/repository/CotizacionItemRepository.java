package com.proarte.erp.cotizaciones.repository;

import com.proarte.erp.cotizaciones.entity.CotizacionItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CotizacionItemRepository extends JpaRepository<CotizacionItem, UUID> {

    List<CotizacionItem> findByCotizacionId(UUID cotizacionId);

    void deleteByCotizacionId(UUID cotizacionId);
}
