package com.proarte.erp.mensajes.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.mensajes.dto.CreateMensajeRequest;
import com.proarte.erp.mensajes.dto.UpdateMensajeRequest;
import com.proarte.erp.mensajes.entity.Mensaje;
import com.proarte.erp.mensajes.repository.MensajeRepository;
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
class MensajeServiceTest {

    @Mock
    private MensajeRepository mensajeRepository;

    @InjectMocks
    private MensajeService mensajeService;

    private Mensaje createTestMensaje() {
        Mensaje mensaje = Mensaje.builder()
                .nombre("Bienvenida")
                .contenido("Hola, gracias por contactarnos")
                .build();
        mensaje.setId(UUID.randomUUID());
        mensaje.setActivo(true);
        return mensaje;
    }

    @Test
    @DisplayName("getAll sin filtro retorna todos")
    void shouldReturnAll_whenNoSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        when(mensajeRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(createTestMensaje())));

        Page<Mensaje> result = mensajeService.getAll(null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAll filtra por nombre cuando search no es null")
    void shouldFilterByNombre() {
        Pageable pageable = PageRequest.of(0, 10);
        when(mensajeRepository.searchByNombre("Bien", pageable)).thenReturn(new PageImpl<>(List.of()));

        mensajeService.getAll("Bien", pageable);

        verify(mensajeRepository).searchByNombre("Bien", pageable);
    }

    @Test
    @DisplayName("getById retorna mensaje cuando existe")
    void shouldReturnMensaje_whenExists() {
        Mensaje mensaje = createTestMensaje();
        when(mensajeRepository.findById(mensaje.getId())).thenReturn(Optional.of(mensaje));

        Mensaje result = mensajeService.getById(mensaje.getId());

        assertThat(result.getNombre()).isEqualTo("Bienvenida");
    }

    @Test
    @DisplayName("getById lanza excepcion cuando no existe")
    void shouldThrowNotFound_whenMensajeDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(mensajeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mensajeService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("create crea mensaje correctamente")
    void shouldCreateMensaje() {
        CreateMensajeRequest request = new CreateMensajeRequest("Nuevo Mensaje", "Contenido del mensaje");
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> {
            Mensaje m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        Mensaje result = mensajeService.create(request);

        assertThat(result.getNombre()).isEqualTo("Nuevo Mensaje");
        assertThat(result.getActivo()).isTrue();
    }

    @Test
    @DisplayName("update actualiza campos no-null")
    void shouldUpdateMensaje() {
        UUID id = UUID.randomUUID();
        Mensaje existing = createTestMensaje();
        existing.setId(id);

        UpdateMensajeRequest request = new UpdateMensajeRequest("Nombre Actualizado", null);

        when(mensajeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(mensajeRepository.save(any(Mensaje.class))).thenAnswer(inv -> inv.getArgument(0));

        Mensaje result = mensajeService.update(id, request);

        assertThat(result.getNombre()).isEqualTo("Nombre Actualizado");
        assertThat(result.getContenido()).isEqualTo("Hola, gracias por contactarnos");
    }

    @Test
    @DisplayName("delete realiza soft-delete cuando existe")
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        when(mensajeRepository.existsActiveById(id)).thenReturn(true);

        mensajeService.delete(id);

        verify(mensajeRepository).softDelete(id);
    }

    @Test
    @DisplayName("delete lanza excepcion cuando no existe")
    void shouldThrowNotFound_whenDeleteNonExistent() {
        UUID id = UUID.randomUUID();
        when(mensajeRepository.existsActiveById(id)).thenReturn(false);

        assertThatThrownBy(() -> mensajeService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
