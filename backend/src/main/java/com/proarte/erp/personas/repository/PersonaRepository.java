package com.proarte.erp.personas.repository;

import com.proarte.erp.common.repository.SoftDeleteRepository;
import com.proarte.erp.personas.entity.Persona;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonaRepository extends SoftDeleteRepository<Persona> {

    @Query("SELECT p FROM Persona p WHERE LOWER(p.nombres) LIKE LOWER(CONCAT('%', :nombre, '%')) OR LOWER(p.apellidos) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Persona> searchByNombre(@Param("nombre") String nombre, Pageable pageable);

    @Query("SELECT p FROM Persona p WHERE p.documento = :documento")
    Page<Persona> searchByDocumento(@Param("documento") String documento, Pageable pageable);

    @Query("SELECT p FROM Persona p WHERE LOWER(p.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<Persona> searchByEmail(@Param("email") String email, Pageable pageable);
}
