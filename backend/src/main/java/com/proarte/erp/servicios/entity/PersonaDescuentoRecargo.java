package com.proarte.erp.servicios.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "persona_descuento_recargo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaDescuentoRecargo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "persona_id", nullable = false)
    private UUID personaId;

    @Column(name = "descuento_recargo_id", nullable = false)
    private UUID descuentoRecargoId;
}
