package com.proarte.erp.roles.dto;

import com.proarte.erp.roles.entity.Permiso;
import com.proarte.erp.roles.entity.Rol;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public record RolResponse(
        UUID id,
        String nombre,
        String descripcion,
        Map<String, Map<String, Boolean>> configuracion
) {
    public static RolResponse of(Rol rol, Permiso permiso) {
        return new RolResponse(
                rol.getId(),
                rol.getNombre(),
                rol.getDescripcion(),
                permiso != null && permiso.getConfiguracion() != null
                        ? permiso.getConfiguracion()
                        : Collections.emptyMap()
        );
    }

    public static RolResponse of(Rol rol, Map<String, Map<String, Boolean>> configuracion) {
        return new RolResponse(
                rol.getId(),
                rol.getNombre(),
                rol.getDescripcion(),
                configuracion != null ? configuracion : Collections.emptyMap()
        );
    }
}
