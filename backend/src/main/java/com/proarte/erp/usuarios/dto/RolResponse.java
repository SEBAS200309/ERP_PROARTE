package com.proarte.erp.usuarios.dto;

import com.proarte.erp.auth.entity.Rol;

import java.util.UUID;

public record RolResponse(
        UUID id,
        String nombre,
        String descripcion,
        Boolean activo
) {

    public static RolResponse from(Rol rol) {
        return new RolResponse(
                rol.getId(),
                rol.getNombre(),
                rol.getDescripcion(),
                rol.getActivo()
        );
    }
}
