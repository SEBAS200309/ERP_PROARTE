package com.proarte.erp.usuarios.dto;

import com.proarte.erp.auth.entity.Usuario;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String username,
        String nombreCompleto,
        String email,
        UUID rolId,
        String rolNombre,
        Boolean activo,
        OffsetDateTime createdAt
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRolId(),
                usuario.getRol != null ? usuario.getRol().getNombre() : null,
                usuario.getActivo(),
                usuario.getCreatedAt()
        );
    }
}
