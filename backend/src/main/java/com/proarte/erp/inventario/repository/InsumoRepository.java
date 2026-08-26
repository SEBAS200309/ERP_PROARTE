package com.proarte.erp.inventario.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.inventario.entity.Insumo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InsumoRepository extends SoftDeleteRepository<Insumo> {

    @Query("SELECT i FROM Insumo i WHERE LOWER(i.nombre) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Insumo> searchByNombre(@Param("search") String search, Pageable pageable);
}
