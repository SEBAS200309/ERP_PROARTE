package com.proarte.erp.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails {

    private final UUID id;
    private final UUID rolId;
    private final String username;
    private final String password;
    private final String nombreCompleto;
    private final String rolNombre;
    private final Map<String, Map<String, Boolean>> permisos;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(UUID id,
            UUID rolId,
            String username,
            String password,
            String nombreCompleto,
            String rolNombre,
            Map<String, Map<String, Boolean>> permisos,
            boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.rolId = rolId;
        this.rolNombre = rolNombre;
        this.permisos = permisos;
        this.enabled = enabled;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rolNombre.toUpperCase()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
