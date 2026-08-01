package com.proarte.erp.mensajes.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.mensajes.entity.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MensajeRepository extends SoftDeleteRepository<Mensaje> {

    @Query("SELECT m FROM Mensaje m WHERE LOWER(m.nombre) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Mensaje> searchByNombre(@Param("search") String search, Pageable pageable);
}
