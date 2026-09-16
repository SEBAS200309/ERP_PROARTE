package com.proarte.erp.auth.repository;

import com.proarte.erp.auth.entity.Usuario;
import com.proarte.erp.common.repository.SoftDeleteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends SoftDeleteRepository<Usuario> {

    Optional<Usuario> findByUsername(String username);

    @Query(value = "SELECT u FROM Usuario u JOIN FETCH u.rol", countQuery = "SELECT COUNT(u) FROM Usuario u")
    Page<Usuario> findAllWithRol(Pageable pageable);

    boolean existsByRolIdAndActivoTrue(UUID rolId);
}
