package com.proarte.erp.eventos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "evento_proveedor")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoProveedor {

    @Id
    @NonNull 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "evento_id", nullable = false)
    @NonNull 
    private UUID eventoId;

    @Column(name = "proveedor_id", nullable = false)
    @NonNull 
    private UUID proveedorId;

    @Column(name = "servicio_id")
    @NonNull 
    private UUID servicioId;
}
