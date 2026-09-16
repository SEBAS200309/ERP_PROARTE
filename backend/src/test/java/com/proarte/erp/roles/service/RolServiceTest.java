package com.proarte.erp.roles.service;

import com.proarte.erp.auth.repository.UsuarioRepository;
import com.proarte.erp.exception.BusinessException;
import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.roles.dto.CreateRolRequest;
import com.proarte.erp.roles.dto.RolResponse;
import com.proarte.erp.roles.dto.UpdateRolRequest;
import com.proarte.erp.roles.entity.Permiso;
import com.proarte.erp.roles.entity.Rol;
import com.proarte.erp.roles.repository.PermisoRepository;
import com.proarte.erp.roles.repository.RolRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PermisoRepository permisoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private RolServiceImpl rolService;

    private Rol createTestRol(UUID id, String nombre) {
        Rol rol = Rol.builder()
                .nombre(nombre)
                .descripcion("Descripción de " + nombre)
                .build();
        rol.setId(id);
        rol.setActivo(true);
        return rol;
    }

    private Permiso createTestPermiso(UUID rolId) {
        Permiso permiso = Permiso.builder()
                .rolId(rolId)
                .configuracion(Map.of("roles", Map.of("leer", true, "crear", true)))
                .build();
        permiso.setId(UUID.randomUUID());
        permiso.setActivo(true);
        return permiso;
    }

    @Test
    @DisplayName("getAll retorna lista de roles con su configuración de permisos")
    void shouldReturnAllRolesWithPermissions() {
        UUID id = UUID.randomUUID();
        Rol rol = createTestRol(id, "Administrador");
        Permiso permiso = createTestPermiso(id);

        when(rolRepository.findAll()).thenReturn(List.of(rol));
        when(permisoRepository.findAll()).thenReturn(List.of(permiso));

        List<RolResponse> result = rolService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(id);
        assertThat(result.get(0).nombre()).isEqualTo("Administrador");
        assertThat(result.get(0).configuracion()).containsKey("roles");
    }

    @Test
    @DisplayName("getById retorna RolResponse cuando el rol existe")
    void shouldReturnRolResponse_whenRolExists() {
        UUID id = UUID.randomUUID();
        Rol rol = createTestRol(id, "Supervisor");
        Permiso permiso = createTestPermiso(id);

        when(rolRepository.findById(id)).thenReturn(Optional.of(rol));
        when(permisoRepository.findByRolId(id)).thenReturn(Optional.of(permiso));

        RolResponse result = rolService.getById(id);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.nombre()).isEqualTo("Supervisor");
        assertThat(result.configuracion()).containsKey("roles");
    }

    @Test
    @DisplayName("getById lanza ResourceNotFoundException cuando el rol no existe")
    void shouldThrowNotFound_whenRolDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(rolRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> rolService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rol");
    }

    @Test
    @DisplayName("create guarda Rol y Permiso de manera atómica")
    void shouldCreateRolAndPermisoAtomically() {
        UUID generatedId = UUID.randomUUID();
        Map<String, Map<String, Boolean>> config = Map.of("usuarios", Map.of("crear", true));
        CreateRolRequest request = new CreateRolRequest("Contador", "Rol contable", config);

        when(rolRepository.existsByNombreIgnoreCaseAndActivoTrue("Contador")).thenReturn(false);
        when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> {
            Rol r = inv.getArgument(0);
            r.setId(generatedId);
            return r;
        });
        when(permisoRepository.save(any(Permiso.class))).thenAnswer(inv -> inv.getArgument(0));

        RolResponse result = rolService.create(request);

        assertThat(result.id()).isEqualTo(generatedId);
        assertThat(result.nombre()).isEqualTo("Contador");
        assertThat(result.configuracion()).containsKey("usuarios");
        verify(rolRepository).save(any(Rol.class));
        verify(permisoRepository).save(any(Permiso.class));
    }

    @Test
    @DisplayName("create lanza BusinessException cuando el nombre ya existe")
    void shouldThrowBusinessException_whenRolNameAlreadyExists() {
        CreateRolRequest request = new CreateRolRequest("Admin", "Desc", Map.of());
        when(rolRepository.existsByNombreIgnoreCaseAndActivoTrue("Admin")).thenReturn(true);

        assertThatThrownBy(() -> rolService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ya existe un rol activo");
    }

    @Test
    @DisplayName("update modifica datos de rol y configuración de permisos")
    void shouldUpdateRolAndPermissions() {
        UUID id = UUID.randomUUID();
        Rol existing = createTestRol(id, "NombreViejo");
        Permiso existingPermiso = createTestPermiso(id);

        Map<String, Map<String, Boolean>> newConfig = Map.of("eventos", Map.of("leer", true));
        UpdateRolRequest request = new UpdateRolRequest("NombreNuevo", "Nueva desc", newConfig);

        when(rolRepository.findById(id)).thenReturn(Optional.of(existing));
        when(rolRepository.findByNombre("NombreNuevo")).thenReturn(Optional.empty());
        when(rolRepository.save(any(Rol.class))).thenAnswer(inv -> inv.getArgument(0));
        when(permisoRepository.findByRolId(id)).thenReturn(Optional.of(existingPermiso));
        when(permisoRepository.save(any(Permiso.class))).thenAnswer(inv -> inv.getArgument(0));

        RolResponse result = rolService.update(id, request);

        assertThat(result.nombre()).isEqualTo("NombreNuevo");
        assertThat(result.descripcion()).isEqualTo("Nueva desc");
        assertThat(result.configuracion()).containsKey("eventos");
    }

    @Test
    @DisplayName("delete soft-deletea Permiso y Rol cuando no tiene usuarios asignados")
    void shouldDeleteRolAndPermiso_whenNoUsersAssigned() {
        UUID id = UUID.randomUUID();
        when(rolRepository.existsActiveById(id)).thenReturn(true);
        when(usuarioRepository.existsByRolIdAndActivoTrue(id)).thenReturn(false);

        rolService.delete(id);

        verify(permisoRepository).softDeleteByRolId(id);
        verify(rolRepository).softDelete(id);
    }

    @Test
    @DisplayName("delete lanza BusinessException si el rol tiene usuarios activos asignados")
    void shouldThrowBusinessException_whenRolHasActiveUsers() {
        UUID id = UUID.randomUUID();
        when(rolRepository.existsActiveById(id)).thenReturn(true);
        when(usuarioRepository.existsByRolIdAndActivoTrue(id)).thenReturn(true);

        assertThatThrownBy(() -> rolService.delete(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("usuarios activos asignados");

        verify(permisoRepository, never()).softDeleteByRolId(any());
        verify(rolRepository, never()).softDelete(any());
    }

    @Test
    @DisplayName("delete lanza ResourceNotFoundException cuando el rol no existe")
    void shouldThrowNotFound_whenDeletingNonExistentRol() {
        UUID id = UUID.randomUUID();
        when(rolRepository.existsActiveById(id)).thenReturn(false);

        assertThatThrownBy(() -> rolService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rol");
    }
}
