package com.proarte.erp.servicios.repository;

import com.proarte.erp.servicios.entity.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ServicioRepository extends JpaRepository<Servicio, UUID>, JpaSpecificationExecutor<Servicio> {

    @Query("SELECT s FROM Servicio s WHERE LOWER(s.nombre) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Servicio> searchByNombre(@Param("search") String search, Pageable pageable);

    Page<Servicio> findByCategoriaId(UUID categoriaId, Pageable pageable);

    @Query("SELECT s FROM Servicio s WHERE LOWER(s.nombre) LIKE LOWER(CONCAT('%', :search, '%')) AND s.categoriaId = :categoriaId")
    Page<Servicio> searchByNombreAndCategoriaId(@Param("search") String search, @Param("categoriaId") UUID categoriaId, Pageable pageable);

    List<Servicio> findByServicioPadreId(UUID servicioPadreId);

    @Modifying
    @Query("UPDATE Servicio s SET s.activo = false, s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :id AND s.activo = true")
    void softDelete(@Param("id") UUID id);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Servicio s WHERE s.id = :id AND s.activo = true")
    boolean existsActiveById(@Param("id") UUID id);
}
