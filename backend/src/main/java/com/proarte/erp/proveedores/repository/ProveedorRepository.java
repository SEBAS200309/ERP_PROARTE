package com.proarte.erp.proveedores.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.proveedores.entity.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProveedorRepository extends SoftDeleteRepository<Proveedor> {

    @Query("SELECT p FROM Proveedor p WHERE LOWER(p.especialidad) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Proveedor> searchByEspecialidad(@Param("search") String search, Pageable pageable);

    Page<Proveedor> findByPersonaId(UUID personaId, Pageable pageable);

    Page<Proveedor> findByEmpresaId(UUID empresaId, Pageable pageable);
}
