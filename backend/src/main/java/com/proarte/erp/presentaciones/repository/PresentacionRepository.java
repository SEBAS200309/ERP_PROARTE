package com.proarte.erp.presentaciones.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.presentaciones.entity.Presentacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PresentacionRepository extends SoftDeleteRepository<Presentacion> {

    @Query("SELECT p FROM Presentacion p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Presentacion> searchByNombre(@Param("search") String search, Pageable pageable);

    Page<Presentacion> findByServicioId(UUID servicioId, Pageable pageable);
}
