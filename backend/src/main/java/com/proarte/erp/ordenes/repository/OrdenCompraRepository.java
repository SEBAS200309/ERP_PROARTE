package com.proarte.erp.ordenes.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.ordenes.entity.OrdenCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrdenCompraRepository extends SoftDeleteRepository<OrdenCompra> {

    Page<OrdenCompra> findByEstadoId(UUID estadoId, Pageable pageable);

    Page<OrdenCompra> findBySolicitudId(UUID solicitudId, Pageable pageable);

    @Query("SELECT o FROM OrdenCompra o WHERE LOWER(o.codigo) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<OrdenCompra> searchByCodigo(@Param("search") String search, Pageable pageable);

    List<OrdenCompra> findAllByIdIn(List<UUID> ids);

    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(o.codigo FROM 'OC-\\d{4}-(\\d+)') AS INTEGER)), 0) FROM orden_compra o WHERE o.codigo LIKE :prefix", nativeQuery = true)
    Integer findMaxCodigoSequence(@Param("prefix") String prefix);
}
