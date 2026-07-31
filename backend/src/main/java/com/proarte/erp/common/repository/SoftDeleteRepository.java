package com.proarte.erp.common.repository;

import com.proarte.erp.common.entity.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * Base repository that provides soft-delete support.
 * All entities using this repository must extend {@link BaseEntity}.
 * <p>
 * Note: With {@code @SQLRestriction("activo = true")} on entities,
 * standard JPA queries (findAll, findById) automatically filter inactive records.
 * This repository adds the explicit {@code softDelete} operation.
 */
@NoRepositoryBean
public interface SoftDeleteRepository<T extends BaseEntity>
        extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {

    /**
     * Performs a logical delete by setting activo = false.
     * The record remains in the database but is excluded from queries
     * due to the @SQLRestriction annotation on the entity.
     *
     * @param id the UUID of the entity to soft-delete
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.activo = false, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id AND e.activo = true")
    void softDelete(@Param("id") UUID id);

    /**
     * Restores a soft-deleted record by setting activo = true.
     *
     * @param id the UUID of the entity to restore
     */
    @Modifying
    @Query(value = "UPDATE #{#entityName} e SET e.activo = true, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id AND e.activo = false")
    void restore(@Param("id") UUID id);

    /**
     * Checks if an active record exists with the given ID.
     *
     * @param id the UUID to check
     * @return true if an active record exists
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM #{#entityName} e WHERE e.id = :id AND e.activo = true")
    boolean existsActiveById(@Param("id") UUID id);
}
