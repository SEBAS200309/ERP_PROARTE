package com.proarte.erp.proveedores.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "portafolio")
@SQLRestriction("activo = true")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portafolio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "proveedor_id", nullable = false)
    private UUID proveedorId;

    @Column(name = "servicio_id", nullable = false)
    private UUID servicioId;

    @Column(name = "precio_unitario", precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}
