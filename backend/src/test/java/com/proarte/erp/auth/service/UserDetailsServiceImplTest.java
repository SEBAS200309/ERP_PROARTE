package com.proarte.erp.auth.service;

import com.proarte.erp.auth.entity.Permiso;
import com.proarte.erp.auth.entity.Rol;
import com.proarte.erp.auth.entity.Usuario;
import com.proarte.erp.auth.repository.PermisoRepository;
import com.proarte.erp.auth.repository.UsuarioRepository;
import com.proarte.erp.security.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PermisoRepository permisoRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("loadUserByUsername retorna CustomUserDetails con permisos cuando usuario existe")
    void shouldLoadUser_whenUsernameExists() {
        UUID userId = UUID.randomUUID();
        UUID rolId = UUID.randomUUID();

        Rol rol = Rol.builder().nombre("Administrador").build();
        rol.setId(rolId);

        Usuario usuario = Usuario.builder()
                .username("admin")
                .passwordHash("encoded")
                .nombreCompleto("Admin User")
                .rolId(rolId)
                .build();
        usuario.setId(userId);
        usuario.setActivo(true);
        // Set the rol via reflection or directly since it's a @ManyToOne
        usuario.setRol(rol);

        Map<String, Map<String, Boolean>> config = Map.of(
                "usuarios", Map.of("leer", true, "crear", true, "editar", false)
        );
        Permiso permiso = Permiso.builder().rolId(rolId).configuracion(config).build();

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(permisoRepository.findByRolId(rolId)).thenReturn(List.of(permiso));

        UserDetails result = userDetailsService.loadUserByUsername("admin");

        assertThat(result).isInstanceOf(CustomUserDetails.class);
        CustomUserDetails details = (CustomUserDetails) result;
        assertThat(details.getId()).isEqualTo(userId);
        assertThat(details.getUsername()).isEqualTo("admin");
        assertThat(details.getNombreCompleto()).isEqualTo("Admin User");
        assertThat(details.getRolNombre()).isEqualTo("Administrador");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.getPermisos()).containsKey("usuarios");
        assertThat(details.getPermisos().get("usuarios").get("leer")).isTrue();
    }

    @Test
    @DisplayName("loadUserByUsername lanza UsernameNotFoundException cuando usuario no existe")
    void shouldThrowException_whenUsernameNotFound() {
        when(usuarioRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("noexiste"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("noexiste");
    }

    @Test
    @DisplayName("loadUserByUsername retorna permisos vacios cuando no hay permisos configurados")
    void shouldReturnEmptyPermisos_whenNoPermisosExist() {
        UUID userId = UUID.randomUUID();
        UUID rolId = UUID.randomUUID();

        Rol rol = Rol.builder().nombre("Viewer").build();
        rol.setId(rolId);

        Usuario usuario = Usuario.builder()
                .username("viewer")
                .passwordHash("encoded")
                .nombreCompleto("Viewer User")
                .rolId(rolId)
                .build();
        usuario.setId(userId);
        usuario.setActivo(true);
        usuario.setRol(rol);

        when(usuarioRepository.findByUsername("viewer")).thenReturn(Optional.of(usuario));
        when(permisoRepository.findByRolId(rolId)).thenReturn(List.of());

        UserDetails result = userDetailsService.loadUserByUsername("viewer");
        CustomUserDetails details = (CustomUserDetails) result;

        assertThat(details.getPermisos()).isEmpty();
    }
}
