package com.proarte.erp.inventario.repository;

import com.proarte.erp.inventario.entity.InsumoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InsumoMovimientoRepository extends JpaRepository<InsumoMovimiento, UUID> {

    Page<InsumoMovimiento> findByInsumoIdOrderByFechaDesc(UUID insumoId, Pageable pageable);

    Page<InsumoMovimiento> findByInsumoIdAndTipoMovimientoOrderByFechaDesc(UUID insumoId, String tipoMovimiento, Pageable pageable);
}
