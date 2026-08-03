package com.proarte.erp.empresas.service;

import com.proarte.erp.empresas.dto.CreateEmpresaRequest;
import com.proarte.erp.empresas.dto.UpdateEmpresaRequest;
import com.proarte.erp.empresas.entity.Empresa;
import com.proarte.erp.empresas.repository.EmpresaRepository;
import com.proarte.erp.exception.ResourceNotFoundException;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaService empresaService;

    private Empresa createTestEmpresa() {
        Empresa empresa = Empresa.builder()
                .razonSocial("Pro Arte SAS")
                .nit("900123456-1")
                .email("info@proarte.com")
                .build();
        empresa.setId(UUID.randomUUID());
        empresa.setActivo(true);
        return empresa;
    }

    @Test
    @DisplayName("getAll sin filtros retorna pagina completa")
    void shouldReturnAll_whenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Empresa> expected = new PageImpl<>(List.of(createTestEmpresa()));
        when(empresaRepository.findAll(pageable)).thenReturn(expected);

        Page<Empresa> result = empresaService.getAll(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAll filtra por razonSocial")
    void shouldFilterByRazonSocial() {
        Pageable pageable = PageRequest.of(0, 10);
        when(empresaRepository.searchByRazonSocial("Pro Arte", pageable)).thenReturn(new PageImpl<>(List.of()));

        empresaService.getAll("Pro Arte", null, pageable);

        verify(empresaRepository).searchByRazonSocial("Pro Arte", pageable);
    }

    @Test
    @DisplayName("getAll filtra por NIT cuando razonSocial es null")
    void shouldFilterByNit() {
        Pageable pageable = PageRequest.of(0, 10);
        when(empresaRepository.searchByNit("900", pageable)).thenReturn(new PageImpl<>(List.of()));

        empresaService.getAll(null, "900", pageable);

        verify(empresaRepository).searchByNit("900", pageable);
    }

    @Test
    @DisplayName("getById retorna empresa cuando existe")
    void shouldReturnEmpresa_whenExists() {
        Empresa empresa = createTestEmpresa();
        when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));

        Empresa result = empresaService.getById(empresa.getId());

        assertThat(result.getRazonSocial()).isEqualTo("Pro Arte SAS");
    }

    @Test
    @DisplayName("getById lanza ResourceNotFoundException cuando no existe")
    void shouldThrowNotFound_whenEmpresaDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(empresaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> empresaService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("create crea empresa correctamente")
    void shouldCreateEmpresa() {
        CreateEmpresaRequest request = new CreateEmpresaRequest("Nueva Empresa", "800111222-3", "Dir 1", "300111", "e@e.com", UUID.randomUUID());
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> {
            Empresa e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        Empresa result = empresaService.create(request);

        assertThat(result.getRazonSocial()).isEqualTo("Nueva Empresa");
        assertThat(result.getActivo()).isTrue();
    }

    @Test
    @DisplayName("update actualiza campos no-null")
    void shouldUpdateEmpresa() {
        UUID id = UUID.randomUUID();
        Empresa existing = createTestEmpresa();
        existing.setId(id);

        UpdateEmpresaRequest request = new UpdateEmpresaRequest("Nombre Actualizado", null, null, null, null, null);

        when(empresaRepository.findById(id)).thenReturn(Optional.of(existing));
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));

        Empresa result = empresaService.update(id, request);

        assertThat(result.getRazonSocial()).isEqualTo("Nombre Actualizado");
    }

    @Test
    @DisplayName("delete realiza soft-delete cuando empresa existe")
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        when(empresaRepository.existsActiveById(id)).thenReturn(true);

        empresaService.delete(id);

        verify(empresaRepository).softDelete(id);
    }

    @Test
    @DisplayName("delete lanza ResourceNotFoundException cuando no existe")
    void shouldThrowNotFound_whenDeleteNonExistent() {
        UUID id = UUID.randomUUID();
        when(empresaRepository.existsActiveById(id)).thenReturn(false);

        assertThatThrownBy(() -> empresaService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("asignarRol actualiza rolEntidadId correctamente")
    void shouldAsignarRol() {
        UUID empresaId = UUID.randomUUID();
        UUID rolId = UUID.randomUUID();
        Empresa empresa = createTestEmpresa();
        empresa.setId(empresaId);

        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(any(Empresa.class))).thenAnswer(inv -> inv.getArgument(0));

        Empresa result = empresaService.asignarRol(empresaId, rolId);

        assertThat(result.getRolEntidadId()).isEqualTo(rolId);
    }
}
