package com.proarte.erp.servicios.repository;

import com.proarte.erp.servicios.entity.EmpresaDescuentoRecargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmpresaDescuentoRecargoRepository extends JpaRepository<EmpresaDescuentoRecargo, UUID> {

    boolean existsByEmpresaIdAndDescuentoRecargoId(UUID empresaId, UUID descuentoRecargoId);
}
