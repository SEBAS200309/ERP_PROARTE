package com.proarte.erp.eventos.service;

import com.proarte.erp.eventos.dto.*;
import com.proarte.erp.eventos.entity.*;
import com.proarte.erp.eventos.repository.*;
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
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private EventoContactoRepository eventoContactoRepository;

    @Mock
    private EventoProveedorRepository eventoProveedorRepository;

    @Mock
    private EventoObservacionRepository eventoObservacionRepository;

    @Mock
    private EventoInsumoRepository eventoInsumoRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private EventoService eventoService;

    private Evento createTestEvento() {
        Evento evento = Evento.builder()
                .nombre("Evento Corporativo")
                .lugar("Hotel ABC")
                .estadoId(UUID.randomUUID())
                .fechaInicio(OffsetDateTime.now())
                .fechaFin(OffsetDateTime.now().plusHours(5))
                .build();
        evento.setId(UUID.randomUUID());
        evento.setActivo(true);
        return evento;
    }

    @Test
    @DisplayName("getAll sin filtros retorna todos")
    void shouldReturnAll_whenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        when(eventoRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(createTestEvento())));

        Page<Evento> result = eventoService.getAll(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAll filtra por nombre")
    void shouldFilterBySearch() {
        Pageable pageable = PageRequest.of(0, 10);
        when(eventoRepository.searchByNombre("Corp", pageable)).thenReturn(new PageImpl<>(List.of()));

        eventoService.getAll("Corp", null, pageable);

        verify(eventoRepository).searchByNombre("Corp", pageable);
    }

    @Test
    @DisplayName("getById retorna evento cuando existe")
    void shouldReturnEvento_whenExists() {
        Evento evento = createTestEvento();
        when(eventoRepository.findById(evento.getId())).thenReturn(Optional.of(evento));

        Evento result = eventoService.getById(evento.getId());

        assertThat(result.getNombre()).isEqualTo("Evento Corporativo");
    }

    @Test
    @DisplayName("getById lanza excepcion cuando no existe")
    void shouldThrowNotFound_whenEventoDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(eventoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("create crea evento correctamente")
    void shouldCreateEvento() {
        CreateEventoRequest request = new CreateEventoRequest(
                UUID.randomUUID(), "Boda", OffsetDateTime.now(), OffsetDateTime.now().plusHours(8), "Salon", UUID.randomUUID()
        );
        when(eventoRepository.save(any(Evento.class))).thenAnswer(inv -> {
            Evento e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        Evento result = eventoService.create(request);

        assertThat(result.getNombre()).isEqualTo("Boda");
        assertThat(result.getActivo()).isTrue();
    }

    @Test
    @DisplayName("delete realiza soft-delete")
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        when(eventoRepository.existsActiveById(id)).thenReturn(true);

        eventoService.delete(id);

        verify(eventoRepository).softDelete(id);
    }

    @Test
    @DisplayName("delete lanza excepcion cuando no existe")
    void shouldThrowNotFound_whenDeleteNonExistent() {
        UUID id = UUID.randomUUID();
        when(eventoRepository.existsActiveById(id)).thenReturn(false);

        assertThatThrownBy(() -> eventoService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("addContacto agrega contacto al evento")
    void shouldAddContacto() {
        UUID eventoId = UUID.randomUUID();
        EventoContactoRequest request = new EventoContactoRequest(UUID.randomUUID(), UUID.randomUUID(), "Obs");

        when(eventoRepository.existsActiveById(eventoId)).thenReturn(true);
        when(eventoContactoRepository.save(any(EventoContacto.class))).thenAnswer(inv -> {
            EventoContacto c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        EventoContacto result = eventoService.addContacto(eventoId, request);

        assertThat(result.getEventoId()).isEqualTo(eventoId);
    }

    @Test
    @DisplayName("addContacto lanza excepcion cuando evento no existe")
    void shouldThrowNotFound_whenEventoDoesNotExistForContacto() {
        UUID eventoId = UUID.randomUUID();
        when(eventoRepository.existsActiveById(eventoId)).thenReturn(false);

        assertThatThrownBy(() -> eventoService.addContacto(eventoId, new EventoContactoRequest(UUID.randomUUID(), null, null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("addObservacion agrega observacion al evento")
    void shouldAddObservacion() {
        UUID eventoId = UUID.randomUUID();
        ObservacionRequest request = new ObservacionRequest("Nota importante");

        when(eventoRepository.existsActiveById(eventoId)).thenReturn(true);
        when(eventoObservacionRepository.save(any(EventoObservacion.class))).thenAnswer(inv -> {
            EventoObservacion o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        EventoObservacion result = eventoService.addObservacion(eventoId, request);

        assertThat(result.getTexto()).isEqualTo("Nota importante");
        assertThat(result.getEventoId()).isEqualTo(eventoId);
    }

    @Test
    @DisplayName("addInsumo agrega insumo al evento")
    void shouldAddInsumo() {
        UUID eventoId = UUID.randomUUID();
        EventoInsumoRequest request = new EventoInsumoRequest(UUID.randomUUID(), BigDecimal.valueOf(10));

        when(eventoRepository.existsActiveById(eventoId)).thenReturn(true);
        when(eventoInsumoRepository.save(any(EventoInsumo.class))).thenAnswer(inv -> {
            EventoInsumo i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        EventoInsumo result = eventoService.addInsumo(eventoId, request);

        assertThat(result.getEventoId()).isEqualTo(eventoId);
    }
}
