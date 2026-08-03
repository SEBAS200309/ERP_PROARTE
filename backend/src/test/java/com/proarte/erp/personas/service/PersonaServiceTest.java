package com.proarte.erp.personas.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.personas.dto.AsociarEmpresaRequest;
import com.proarte.erp.personas.dto.CreatePersonaRequest;
import com.proarte.erp.personas.dto.UpdatePersonaRequest;
import com.proarte.erp.personas.entity.Persona;
import com.proarte.erp.personas.entity.PersonaEmpresa;
import com.proarte.erp.personas.repository.PersonaEmpresaRepository;
import com.proarte.erp.personas.repository.PersonaRepository;
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
class PersonaServiceTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private PersonaEmpresaRepository personaEmpresaRepository;

    @InjectMocks
    private PersonaService personaService;

    private Persona createTestPersona() {
        Persona persona = Persona.builder()
                .nombres("Juan")
                .apellidos("Perez")
                .documento("12345678")
                .email("juan@example.com")
                .build();
        persona.setId(UUID.randomUUID());
        persona.setActivo(true);
        return persona;
    }

    @Test
    @DisplayName("getAll sin filtros retorna pagina completa")
    void shouldReturnAll_whenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Persona> expected = new PageImpl<>(List.of(createTestPersona()));
        when(personaRepository.findAll(pageable)).thenReturn(expected);

        Page<Persona> result = personaService.getAll(null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAll filtra por nombre cuando se proporciona")
    void shouldFilterByNombre() {
        Pageable pageable = PageRequest.of(0, 10);
        when(personaRepository.searchByNombre("Juan", pageable)).thenReturn(new PageImpl<>(List.of()));

        personaService.getAll("Juan", null, null, pageable);

        verify(personaRepository).searchByNombre("Juan", pageable);
    }

    @Test
    @DisplayName("getById retorna persona cuando existe")
    void shouldReturnPersona_whenExists() {
        Persona persona = createTestPersona();
        when(personaRepository.findById(persona.getId())).thenReturn(Optional.of(persona));

        Persona result = personaService.getById(persona.getId());

        assertThat(result.getNombres()).isEqualTo("Juan");
    }

    @Test
    @DisplayName("getById lanza ResourceNotFoundException cuando no existe")
    void shouldThrowNotFound_whenPersonaDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(personaRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personaService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("create crea persona correctamente")
    void shouldCreatePersona() {
        CreatePersonaRequest request = new CreatePersonaRequest(
                "Maria", "Lopez", UUID.randomUUID(), "99887766", "3001234567", "maria@test.com", "Calle 1", UUID.randomUUID()
        );
        when(personaRepository.save(any(Persona.class))).thenAnswer(inv -> {
            Persona p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        Persona result = personaService.create(request);

        assertThat(result.getNombres()).isEqualTo("Maria");
        assertThat(result.getActivo()).isTrue();
    }

    @Test
    @DisplayName("update actualiza campos no-null")
    void shouldUpdatePersona() {
        UUID id = UUID.randomUUID();
        Persona existing = createTestPersona();
        existing.setId(id);

        UpdatePersonaRequest request = new UpdatePersonaRequest("Carlos", null, null, null, null, "new@email.com", null, null);

        when(personaRepository.findById(id)).thenReturn(Optional.of(existing));
        when(personaRepository.save(any(Persona.class))).thenAnswer(inv -> inv.getArgument(0));

        Persona result = personaService.update(id, request);

        assertThat(result.getNombres()).isEqualTo("Carlos");
        assertThat(result.getEmail()).isEqualTo("new@email.com");
    }

    @Test
    @DisplayName("delete realiza soft-delete cuando persona existe")
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        when(personaRepository.existsActiveById(id)).thenReturn(true);

        personaService.delete(id);

        verify(personaRepository).softDelete(id);
    }

    @Test
    @DisplayName("delete lanza ResourceNotFoundException cuando no existe")
    void shouldThrowNotFound_whenDeleteNonExistent() {
        UUID id = UUID.randomUUID();
        when(personaRepository.existsActiveById(id)).thenReturn(false);

        assertThatThrownBy(() -> personaService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("asociarEmpresa crea asociacion cuando persona existe")
    void shouldAsociarEmpresa() {
        UUID personaId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        AsociarEmpresaRequest request = new AsociarEmpresaRequest(empresaId, "Gerente");

        when(personaRepository.existsActiveById(personaId)).thenReturn(true);
        when(personaEmpresaRepository.save(any(PersonaEmpresa.class))).thenAnswer(inv -> {
            PersonaEmpresa pe = inv.getArgument(0);
            pe.setId(UUID.randomUUID());
            return pe;
        });

        PersonaEmpresa result = personaService.asociarEmpresa(personaId, request);

        assertThat(result.getPersonaId()).isEqualTo(personaId);
        assertThat(result.getEmpresaId()).isEqualTo(empresaId);
    }
}
