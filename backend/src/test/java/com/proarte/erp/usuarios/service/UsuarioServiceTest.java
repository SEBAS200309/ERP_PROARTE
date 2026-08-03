package com.proarte.erp.usuarios.service;

import com.proarte.erp.auth.entity.Permiso;
import com.proarte.erp.auth.entity.Usuario;
import com.proarte.erp.auth.repository.PermisoRepository;
import com.proarte.erp.auth.repository.RolRepository;
import com.proarte.erp.auth.repository.UsuarioRepository;
import com.proarte.erp.exception.BusinessException;
import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.usuarios.dto.CreateUsuarioRequest;
import com.proarte.erp.usuarios.dto.UpdateUsuarioRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PermisoRepository permisoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario createTestUsuario() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.builder()
                .username("testuser")
                .passwordHash("encoded_password")
                .nombreCompleto("Test User")
                .email("test@example.com")
                .rolId(UUID.randomUUID())
                .build();
        usuario.setId(id);
        usuario.setActivo(true);
        return usuario;
    }

    @Test
    @DisplayName("getAll retorna pagina de usuarios")
    void shouldReturnPageOfUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> expectedPage = new PageImpl<>(List.of(createTestUsuario()));
        when(usuarioRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Usuario> result = usuarioService.getAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getById retorna usuario cuando existe")
    void shouldReturnUsuario_whenIdExists() {
        Usuario usuario = createTestUsuario();
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        Usuario result = usuarioService.getById(usuario.getId());

        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("getById lanza ResourceNotFoundException cuando no existe")
    void shouldThrowNotFound_whenIdDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuario");
    }

    @Test
    @DisplayName("create crea usuario con password encriptado")
    void shouldCreateUsuario_whenRequestIsValid() {
        UUID rolId = UUID.randomUUID();
        CreateUsuarioRequest request = new CreateUsuarioRequest("newuser", "password123", "New User", "new@example.com", rolId);

        when(usuarioRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(rolRepository.existsActiveById(rolId)).thenReturn(true);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_pass");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        Usuario result = usuarioService.create(request);

        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getPasswordHash()).isEqualTo("encoded_pass");
        assertThat(result.getNombreCompleto()).isEqualTo("New User");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    @DisplayName("create lanza BusinessException cuando username ya existe")
    void shouldThrowBusinessException_whenUsernameAlreadyExists() {
        UUID rolId = UUID.randomUUID();
        CreateUsuarioRequest request = new CreateUsuarioRequest("existing", "pass", "Name", "e@e.com", rolId);

        when(usuarioRepository.findByUsername("existing")).thenReturn(Optional.of(createTestUsuario()));

        assertThatThrownBy(() -> usuarioService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya está en uso");
    }

    @Test
    @DisplayName("create lanza ResourceNotFoundException cuando rol no existe")
    void shouldThrowNotFound_whenRolDoesNotExist() {
        UUID rolId = UUID.randomUUID();
        CreateUsuarioRequest request = new CreateUsuarioRequest("newuser", "pass", "Name", "e@e.com", rolId);

        when(usuarioRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(rolRepository.existsActiveById(rolId)).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rol");
    }

    @Test
    @DisplayName("update actualiza campos proporcionados")
    void shouldUpdateUsuario_whenFieldsProvided() {
        UUID id = UUID.randomUUID();
        UUID newRolId = UUID.randomUUID();
        Usuario existing = createTestUsuario();
        existing.setId(id);

        UpdateUsuarioRequest request = new UpdateUsuarioRequest("Updated Name", "new@email.com", newRolId, null);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existing));
        when(rolRepository.existsActiveById(newRolId)).thenReturn(true);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario result = usuarioService.update(id, request);

        assertThat(result.getNombreCompleto()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("new@email.com");
        assertThat(result.getRolId()).isEqualTo(newRolId);
    }

    @Test
    @DisplayName("update encripta nueva password si se proporciona")
    void shouldEncryptPassword_whenPasswordProvided() {
        UUID id = UUID.randomUUID();
        Usuario existing = createTestUsuario();
        existing.setId(id);

        UpdateUsuarioRequest request = new UpdateUsuarioRequest(null, null, null, "newPassword");

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newPassword")).thenReturn("new_encoded");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario result = usuarioService.update(id, request);

        assertThat(result.getPasswordHash()).isEqualTo("new_encoded");
        verify(passwordEncoder).encode("newPassword");
    }

    @Test
    @DisplayName("delete realiza soft-delete cuando usuario existe")
    void shouldSoftDelete_whenUsuarioExists() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.existsActiveById(id)).thenReturn(true);

        usuarioService.delete(id);

        verify(usuarioRepository).softDelete(id);
    }

    @Test
    @DisplayName("delete lanza ResourceNotFoundException cuando usuario no existe")
    void shouldThrowNotFound_whenDeleteNonExistentUser() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.existsActiveById(id)).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getPermisosByRolId retorna permisos cuando rol existe")
    void shouldReturnPermisos_whenRolExists() {
        UUID rolId = UUID.randomUUID();
        Permiso permiso = Permiso.builder()
                .rolId(rolId)
                .configuracion(Map.of("usuarios", Map.of("leer", true)))
                .build();

        when(rolRepository.existsActiveById(rolId)).thenReturn(true);
        when(permisoRepository.findByRolId(rolId)).thenReturn(List.of(permiso));

        List<Permiso> result = usuarioService.getPermisosByRolId(rolId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getConfiguracion()).containsKey("usuarios");
    }

    @Test
    @DisplayName("getPermisosByRolId lanza ResourceNotFoundException cuando rol no existe")
    void shouldThrowNotFound_whenRolDoesNotExistForPermisos() {
        UUID rolId = UUID.randomUUID();
        when(rolRepository.existsActiveById(rolId)).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.getPermisosByRolId(rolId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rol");
    }

    @Test
    @DisplayName("updatePermisosForRol crea nuevo permiso cuando no existe")
    void shouldCreateNewPermiso_whenNoneExist() {
        UUID rolId = UUID.randomUUID();
        Map<String, Map<String, Boolean>> config = Map.of("eventos", Map.of("leer", true));

        when(rolRepository.existsActiveById(rolId)).thenReturn(true);
        when(permisoRepository.findByRolId(rolId)).thenReturn(List.of());
        when(permisoRepository.save(any(Permiso.class))).thenAnswer(inv -> inv.getArgument(0));

        Permiso result = usuarioService.updatePermisosForRol(rolId, config);

        assertThat(result.getRolId()).isEqualTo(rolId);
        assertThat(result.getConfiguracion()).containsKey("eventos");
    }

    @Test
    @DisplayName("updatePermisosForRol actualiza permiso existente")
    void shouldUpdateExistingPermiso_whenAlreadyExists() {
        UUID rolId = UUID.randomUUID();
        Permiso existing = Permiso.builder()
                .rolId(rolId)
                .configuracion(Map.of("old", Map.of("leer", true)))
                .build();
        existing.setId(UUID.randomUUID());

        Map<String, Map<String, Boolean>> newConfig = Map.of("new", Map.of("crear", true));

        when(rolRepository.existsActiveById(rolId)).thenReturn(true);
        when(permisoRepository.findByRolId(rolId)).thenReturn(List.of(existing));
        when(permisoRepository.save(any(Permiso.class))).thenAnswer(inv -> inv.getArgument(0));

        Permiso result = usuarioService.updatePermisosForRol(rolId, newConfig);

        assertThat(result.getConfiguracion()).containsKey("new");
        assertThat(result.getConfiguracion()).doesNotContainKey("old");
    }
}
