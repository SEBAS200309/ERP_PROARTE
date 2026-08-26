package com.proarte.erp.proveedores.repository;

import com.proarte.erp.proveedores.entity.Portafolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PortafolioRepository extends JpaRepository<Portafolio, UUID>, JpaSpecificationExecutor<Portafolio> {

    List<Portafolio> findByProveedorId(UUID proveedorId);

    @Modifying
    @Query("UPDATE Portafolio p SET p.activo = false WHERE p.id = :id AND p.activo = true")
    void softDelete(@Param("id") UUID id);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Portafolio p WHERE p.id = :id AND p.activo = true")
    boolean existsActiveById(@Param("id") UUID id);
}
