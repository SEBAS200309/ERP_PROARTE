package com.proarte.erp.servicios.repository;

import com.proarte.erp.servicios.entity.EmpresaDescuentoRecargo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmpresaDescuentoRecargoRepository extends JpaRepository<EmpresaDescuentoRecargo, UUID> {

    boolean existsByEmpresaIdAndDescuentoRecargoId(UUID empresaId, UUID descuentoRecargoId);
}
