package com.proarte.erp.cotizaciones.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.cotizaciones.entity.Cotizacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CotizacionRepository extends SoftDeleteRepository<Cotizacion> {

    @Override
    @EntityGraph(attributePaths = { "items" })
    Optional<Cotizacion> findById(UUID id);

    Page<Cotizacion> findByEstadoId(UUID estadoId, Pageable pageable);

    Page<Cotizacion> findByPersonaId(UUID personaId, Pageable pageable);

    Page<Cotizacion> findByEmpresaId(UUID empresaId, Pageable pageable);

    @Query("SELECT c FROM Cotizacion c WHERE LOWER(c.codigo) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Cotizacion> searchByCodigo(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Cotizacion c WHERE c.createdAt >= CAST(:desde AS timestamp)")
    Page<Cotizacion> findByFechaDesde(@Param("desde") java.time.OffsetDateTime desde, Pageable pageable);

    @Query(value = "SELECT * FROM fn_cotizaciones_por_vencer(:dias)", nativeQuery = true)
    List<Cotizacion> findPorVencer(@Param("dias") Integer dias);

    @Query("SELECT COUNT(c) FROM Cotizacion c WHERE c.activo = true")
    long countActive();

    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(c.codigo FROM 'COT-\\d{4}-(\\d+)') AS INTEGER)), 0) FROM cotizacion c WHERE c.codigo LIKE :prefix", nativeQuery = true)
    Integer findMaxCodigoSequence(@Param("prefix") String prefix);

    @Query(value = "SELECT COUNT(c.id) AS cotizaciones_sin_evento FROM cotizacion c WHERE NOT EXISTS ( SELECT 1 FROM evento e WHERE e.cotizacion_id = c.id ) AND c.activo = TRUE;", nativeQuery = true)
    Integer countCotizacionesPendientes();
}
