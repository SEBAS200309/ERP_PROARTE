package com.proarte.erp.auth.repository;

import com.proarte.erp.auth.entity.Usuario;
import com.proarte.erp.common.repository.SoftDeleteRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends SoftDeleteRepository<Usuario> {

    Optional<Usuario> findByUsername(String username);
}
