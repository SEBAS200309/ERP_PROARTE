package com.proarte.erp.servicios.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.servicios.dto.CreateServicioRequest;
import com.proarte.erp.servicios.dto.UpdateServicioRequest;
import com.proarte.erp.servicios.entity.Servicio;
import com.proarte.erp.servicios.repository.ServicioRepository;
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
class ServicioServiceTest {

    @Mock
    private ServicioRepository servicioRepository;

    @InjectMocks
    private ServicioService servicioService;

    private Servicio createTestServicio() {
        Servicio servicio = Servicio.builder()
                .nombre("Sonido Profesional")
                .descripcion("Servicio de sonido para eventos")
                .esPropio(true)
                .requiereOc(false)
                .activo(true)
                .build();
        servicio.setId(UUID.randomUUID());
        return servicio;
    }

    @Test
    @DisplayName("getAllServicios sin filtros retorna todos")
    void shouldReturnAll_whenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        when(servicioRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(createTestServicio())));

        Page<Servicio> result = servicioService.getAllServicios(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAllServicios filtra por search")
    void shouldFilterBySearch() {
        Pageable pageable = PageRequest.of(0, 10);
        when(servicioRepository.searchByNombre("Sonido", pageable)).thenReturn(new PageImpl<>(List.of()));

        servicioService.getAllServicios("Sonido", null, pageable);

        verify(servicioRepository).searchByNombre("Sonido", pageable);
    }

    @Test
    @DisplayName("getAllServicios filtra por categoriaId")
    void shouldFilterByCategoriaId() {
        Pageable pageable = PageRequest.of(0, 10);
        UUID categoriaId = UUID.randomUUID();
        when(servicioRepository.findByCategoriaId(categoriaId, pageable)).thenReturn(new PageImpl<>(List.of()));

        servicioService.getAllServicios(null, categoriaId, pageable);

        verify(servicioRepository).findByCategoriaId(categoriaId, pageable);
    }

    @Test
    @DisplayName("getServicioById retorna servicio cuando existe")
    void shouldReturnServicio_whenExists() {
        Servicio servicio = createTestServicio();
        when(servicioRepository.findById(servicio.getId())).thenReturn(Optional.of(servicio));

        Servicio result = servicioService.getServicioById(servicio.getId());

        assertThat(result.getNombre()).isEqualTo("Sonido Profesional");
    }

    @Test
    @DisplayName("getServicioById lanza excepcion cuando no existe")
    void shouldThrowNotFound_whenServicioDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(servicioRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicioService.getServicioById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createServicio crea servicio con defaults correctos")
    void shouldCreateServicio_withDefaults() {
        CreateServicioRequest request = new CreateServicioRequest("Nuevo Servicio", "Desc", null, null, null, null);
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(inv -> {
            Servicio s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        Servicio result = servicioService.createServicio(request);

        assertThat(result.getNombre()).isEqualTo("Nuevo Servicio");
        assertThat(result.getEsPropio()).isTrue();
        assertThat(result.getRequiereOc()).isFalse();
    }

    @Test
    @DisplayName("deleteServicio realiza soft-delete cuando existe")
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        when(servicioRepository.existsActiveById(id)).thenReturn(true);

        servicioService.deleteServicio(id);

        verify(servicioRepository).softDelete(id);
    }

    @Test
    @DisplayName("deleteServicio lanza excepcion cuando no existe")
    void shouldThrowNotFound_whenDeleteNonExistent() {
        UUID id = UUID.randomUUID();
        when(servicioRepository.existsActiveById(id)).thenReturn(false);

        assertThatThrownBy(() -> servicioService.deleteServicio(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getSubservicios retorna hijos del servicio padre")
    void shouldReturnSubservicios() {
        UUID padreId = UUID.randomUUID();
        Servicio hijo = createTestServicio();
        hijo.setServicioPadreId(padreId);

        when(servicioRepository.existsActiveById(padreId)).thenReturn(true);
        when(servicioRepository.findByServicioPadreId(padreId)).thenReturn(List.of(hijo));

        List<Servicio> result = servicioService.getSubservicios(padreId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("categorizar actualiza categoriaId")
    void shouldCategorizar() {
        UUID servicioId = UUID.randomUUID();
        UUID categoriaId = UUID.randomUUID();
        Servicio servicio = createTestServicio();
        servicio.setId(servicioId);

        when(servicioRepository.findById(servicioId)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(any(Servicio.class))).thenAnswer(inv -> inv.getArgument(0));

        Servicio result = servicioService.categorizar(servicioId, categoriaId);

        assertThat(result.getCategoriaId()).isEqualTo(categoriaId);
    }
}
