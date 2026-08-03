package com.proarte.erp.leads.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.leads.dto.CreateLeadRequest;
import com.proarte.erp.leads.dto.UpdateLeadRequest;
import com.proarte.erp.leads.entity.Lead;
import com.proarte.erp.leads.repository.LeadRepository;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadServiceTest {

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private LeadService leadService;

    private Lead createTestLead() {
        Lead lead = Lead.builder()
                .descripcion("Evento corporativo para 200 personas")
                .estadoId(UUID.randomUUID())
                .personaId(UUID.randomUUID())
                .empresaId(UUID.randomUUID())
                .build();
        lead.setId(UUID.randomUUID());
        lead.setActivo(true);
        return lead;
    }

    @Test
    @DisplayName("getAll sin filtros retorna pagina completa")
    void shouldReturnAll_whenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Lead> expected = new PageImpl<>(List.of(createTestLead()));
        when(leadRepository.findAll(pageable)).thenReturn(expected);

        Page<Lead> result = leadService.getAll(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAll con search filtra por descripcion")
    void shouldFilterBySearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Lead> expected = new PageImpl<>(List.of(createTestLead()));
        when(leadRepository.searchByDescripcion("corporativo", pageable)).thenReturn(expected);

        Page<Lead> result = leadService.getAll("corporativo", null, pageable);

        verify(leadRepository).searchByDescripcion("corporativo", pageable);
    }

    @Test
    @DisplayName("getAll con search y estadoId filtra por ambos")
    void shouldFilterBySearchAndEstadoId() {
        Pageable pageable = PageRequest.of(0, 10);
        UUID estadoId = UUID.randomUUID();
        Page<Lead> expected = new PageImpl<>(List.of());
        when(leadRepository.searchByDescripcionAndEstadoId("test", estadoId, pageable)).thenReturn(expected);

        leadService.getAll("test", estadoId, pageable);

        verify(leadRepository).searchByDescripcionAndEstadoId("test", estadoId, pageable);
    }

    @Test
    @DisplayName("getById retorna lead cuando existe")
    void shouldReturnLead_whenExists() {
        Lead lead = createTestLead();
        when(leadRepository.findById(lead.getId())).thenReturn(Optional.of(lead));

        Lead result = leadService.getById(lead.getId());

        assertThat(result.getDescripcion()).contains("corporativo");
    }

    @Test
    @DisplayName("getById lanza ResourceNotFoundException cuando no existe")
    void shouldThrowNotFound_whenLeadDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(leadRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leadService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lead");
    }

    @Test
    @DisplayName("create crea lead con campos correctos")
    void shouldCreateLead() {
        UUID estadoId = UUID.randomUUID();
        CreateLeadRequest request = new CreateLeadRequest("Nuevo lead", estadoId, null, null);

        when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> {
            Lead l = inv.getArgument(0);
            l.setId(UUID.randomUUID());
            return l;
        });

        Lead result = leadService.create(request);

        assertThat(result.getDescripcion()).isEqualTo("Nuevo lead");
        assertThat(result.getEstadoId()).isEqualTo(estadoId);
        assertThat(result.getActivo()).isTrue();
    }

    @Test
    @DisplayName("update actualiza solo campos no-null")
    void shouldUpdateOnlyNonNullFields() {
        UUID id = UUID.randomUUID();
        Lead existing = createTestLead();
        existing.setId(id);
        UUID originalEstadoId = existing.getEstadoId();

        UpdateLeadRequest request = new UpdateLeadRequest("Descripcion actualizada", null, null, null);

        when(leadRepository.findById(id)).thenReturn(Optional.of(existing));
        when(leadRepository.save(any(Lead.class))).thenAnswer(inv -> inv.getArgument(0));

        Lead result = leadService.update(id, request);

        assertThat(result.getDescripcion()).isEqualTo("Descripcion actualizada");
        assertThat(result.getEstadoId()).isEqualTo(originalEstadoId);
    }

    @Test
    @DisplayName("delete realiza soft-delete cuando lead existe")
    void shouldSoftDelete_whenLeadExists() {
        UUID id = UUID.randomUUID();
        when(leadRepository.existsActiveById(id)).thenReturn(true);

        leadService.delete(id);

        verify(leadRepository).softDelete(id);
    }

    @Test
    @DisplayName("delete lanza ResourceNotFoundException cuando lead no existe")
    void shouldThrowNotFound_whenDeleteNonExistentLead() {
        UUID id = UUID.randomUUID();
        when(leadRepository.existsActiveById(id)).thenReturn(false);

        assertThatThrownBy(() -> leadService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getEstadisticas retorna mapa de estadisticas por estado")
    void shouldReturnEstadisticas() {
        List<Object[]> mockResults = List.of(
                new Object[]{"Nuevo", 5L},
                new Object[]{"En Proceso", 3L},
                new Object[]{"Cerrado", 10L}
        );
        when(leadRepository.countByEstado()).thenReturn(mockResults);

        Map<String, Long> result = leadService.getEstadisticas();

        assertThat(result).hasSize(3);
        assertThat(result.get("Nuevo")).isEqualTo(5L);
        assertThat(result.get("En Proceso")).isEqualTo(3L);
        assertThat(result.get("Cerrado")).isEqualTo(10L);
    }
}
