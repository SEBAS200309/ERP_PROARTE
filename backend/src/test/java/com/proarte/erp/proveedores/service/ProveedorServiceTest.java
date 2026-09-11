package com.proarte.erp.proveedores.service;

import com.proarte.erp.exception.ResourceNotFoundException;
import com.proarte.erp.proveedores.dto.*;
import com.proarte.erp.proveedores.entity.Portafolio;
import com.proarte.erp.proveedores.entity.Proveedor;
import com.proarte.erp.proveedores.entity.SolicitudServicio;
import com.proarte.erp.proveedores.repository.PortafolioRepository;
import com.proarte.erp.proveedores.repository.ProveedorRepository;
import com.proarte.erp.proveedores.repository.SolicitudServicioRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private PortafolioRepository portafolioRepository;

    @Mock
    private SolicitudServicioRepository solicitudServicioRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    private Proveedor createTestProveedor() {
        Proveedor proveedor = Proveedor.builder()
                .personaId(UUID.randomUUID())
                .especialidad("Sonido")
                .build();
        proveedor.setId(UUID.randomUUID());
        proveedor.setActivo(true);
        return proveedor;
    }

    @Test
    @DisplayName("getAllProveedores sin filtro retorna todos")
    void shouldReturnAll_whenNoSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        when(proveedorRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(createTestProveedor())));

        // Se envía 'null' como segundo parámetro para el argumento 'tipo'
        Page<Proveedor> result = proveedorService.getAllProveedores(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("getAllProveedores filtra por especialidad")
    void shouldFilterByEspecialidad() {
        Pageable pageable = PageRequest.of(0, 10);
        when(proveedorRepository.searchByEspecialidad("Sonido", pageable)).thenReturn(new PageImpl<>(List.of()));

        // Se envía 'null' como segundo parámetro para el argumento 'tipo'
        proveedorService.getAllProveedores("Sonido", null, pageable);

        verify(proveedorRepository).searchByEspecialidad("Sonido", pageable);
    }

    @Test
    @DisplayName("getProveedorById retorna proveedor cuando existe")
    void shouldReturnProveedor_whenExists() {
        Proveedor proveedor = createTestProveedor();
        when(proveedorRepository.findById(proveedor.getId())).thenReturn(Optional.of(proveedor));

        Proveedor result = proveedorService.getProveedorById(proveedor.getId());

        assertThat(result.getEspecialidad()).isEqualTo("Sonido");
    }

    @Test
    @DisplayName("getProveedorById lanza excepcion cuando no existe")
    void shouldThrowNotFound_whenProveedorDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(proveedorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> proveedorService.getProveedorById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createProveedor crea proveedor correctamente")
    void shouldCreateProveedor() {
        CreateProveedorRequest request = new CreateProveedorRequest(UUID.randomUUID(), null, "Iluminacion");
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> {
            Proveedor p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        Proveedor result = proveedorService.createProveedor(request);

        assertThat(result.getEspecialidad()).isEqualTo("Iluminacion");
        assertThat(result.getActivo()).isTrue();
    }

    @Test
    @DisplayName("deleteProveedor realiza soft-delete cuando existe")
    void shouldSoftDelete() {
        UUID id = UUID.randomUUID();
        when(proveedorRepository.existsActiveById(id)).thenReturn(true);

        proveedorService.deleteProveedor(id);

        verify(proveedorRepository).softDelete(id);
    }

    @Test
    @DisplayName("deleteProveedor lanza excepcion cuando no existe")
    void shouldThrowNotFound_whenDeleteNonExistent() {
        UUID id = UUID.randomUUID();
        when(proveedorRepository.existsActiveById(id)).thenReturn(false);

        assertThatThrownBy(() -> proveedorService.deleteProveedor(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createPortafolio crea portafolio cuando proveedor existe")
    void shouldCreatePortafolio() {
        UUID proveedorId = UUID.randomUUID();
        CreatePortafolioRequest request = new CreatePortafolioRequest(UUID.randomUUID(), BigDecimal.valueOf(500));

        when(proveedorRepository.existsActiveById(proveedorId)).thenReturn(true);
        when(portafolioRepository.save(any(Portafolio.class))).thenAnswer(inv -> {
            Portafolio p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        Portafolio result = proveedorService.createPortafolio(proveedorId, request);

        assertThat(result.getProveedorId()).isEqualTo(proveedorId);
        assertThat(result.getPrecioUnitario()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    @DisplayName("createPortafolio lanza excepcion cuando proveedor no existe")
    void shouldThrowNotFound_whenProveedorDoesNotExistForPortafolio() {
        UUID proveedorId = UUID.randomUUID();
        when(proveedorRepository.existsActiveById(proveedorId)).thenReturn(false);

        assertThatThrownBy(() -> proveedorService.createPortafolio(proveedorId,
                new CreatePortafolioRequest(UUID.randomUUID(), BigDecimal.TEN)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createSolicitud crea solicitud cuando proveedor existe")
    void shouldCreateSolicitud() {
        UUID proveedorId = UUID.randomUUID();
        CreateSolicitudRequest request = new CreateSolicitudRequest(
                proveedorId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "Descripcion");

        when(proveedorRepository.existsActiveById(proveedorId)).thenReturn(true);
        when(solicitudServicioRepository.save(any(SolicitudServicio.class))).thenAnswer(inv -> {
            SolicitudServicio s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        SolicitudServicio result = proveedorService.createSolicitud(request);

        assertThat(result.getProveedorId()).isEqualTo(proveedorId);
    }
}