package com.proarte.erp.auth.repository;

import com.proarte.erp.auth.entity.Usuario;
import com.proarte.erp.common.repository.SoftDeleteRepository;

import java.util.Optional;

public interface UsuarioRepository extends SoftDeleteRepository<Usuario> {

    Optional<Usuario> findByUsername(String username);
}
