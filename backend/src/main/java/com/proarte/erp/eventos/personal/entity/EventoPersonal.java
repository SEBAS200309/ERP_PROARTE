package com.proarte.erp.eventos.personal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "evento_personal")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoPersonal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "evento_id", nullable = false)
    private UUID eventoId;

    @Column(name = "persona_id", nullable = false)
    private UUID personaId;

    @Column(name = "proveedor_id", nullable = false)
    private UUID proveedorId;

    @Column(name = "servicio_id")
    private UUID servicioId;

    @Column(name = "valor_turno", precision = 12, scale = 2)
    private BigDecimal valorTurno;

    @Column(name = "tiene_arl")
    private Boolean tieneArl;

    @Column(name = "tiene_op")
    private Boolean tieneOp;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
