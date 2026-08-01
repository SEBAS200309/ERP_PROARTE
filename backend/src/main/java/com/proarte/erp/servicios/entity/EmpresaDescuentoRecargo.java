package com.proarte.erp.servicios.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "empresa_descuento_recargo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaDescuentoRecargo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "descuento_recargo_id", nullable = false)
    private UUID descuentoRecargoId;
}
