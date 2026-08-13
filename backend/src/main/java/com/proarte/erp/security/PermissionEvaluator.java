package com.proarte.erp.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component("permissionEvaluator")
public class PermissionEvaluator {

    /**
     * Verifica si el usuario autenticado tiene permiso para una acción en un módulo.
     * Lee los permisos desde el JSONB cargado en el UserDetails al momento del login.
     *
     * @param modulo Nombre del módulo (ej: "usuarios", "eventos")
     * @param accion Acción a verificar (ej: "ver_listado", "ver_detalle", "crear", "editar", "eliminar", "ejecutar")
     * @return true si el usuario tiene permiso, false en caso contrario
     */
    public boolean hasPermission(String modulo, String accion) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return false;
        }

        Map<String, Map<String, Boolean>> permisos = userDetails.getPermisos();
        if (permisos == null) {
            return false;
        }

        Map<String, Boolean> moduloPermisos = permisos.get(modulo);
        if (moduloPermisos == null) {
            return false;
        }

        Boolean permitido = moduloPermisos.get(accion);
        return Boolean.TRUE.equals(permitido);
    }
}
