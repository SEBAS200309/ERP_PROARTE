package com.proarte.erp.empresas.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.empresas.entity.Empresa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends SoftDeleteRepository<Empresa> {

    @Query("SELECT e FROM Empresa e WHERE LOWER(e.razonSocial) LIKE LOWER(CONCAT('%', :razonSocial, '%'))")
    Page<Empresa> searchByRazonSocial(@Param("razonSocial") String razonSocial, Pageable pageable);

    @Query("SELECT e FROM Empresa e WHERE e.nit = :nit")
    Page<Empresa> searchByNit(@Param("nit") String nit, Pageable pageable);
}
