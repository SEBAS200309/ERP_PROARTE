package com.proarte.erp.servicios.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "servicio_descuento_recargo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioDescuentoRecargo {

    @Id
    @NonNull 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "servicio_id", nullable = false)
    @NonNull 
    private UUID servicioId;

    @Column(name = "descuento_recargo_id", nullable = false)
    @NonNull 
    private UUID descuentoRecargoId;
}
