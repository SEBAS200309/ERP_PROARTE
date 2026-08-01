package com.proarte.erp.alimentacion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "evento_alimentacion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EventoAlimentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "evento_id", nullable = false)
    private UUID eventoId;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @Column(name = "cantidad", nullable = false)
    private BigDecimal cantidad;

    @Column(name = "tipo_movimiento", nullable = false, length = 10)
    private String tipoMovimiento;

    @Column(name = "fecha")
    private OffsetDateTime fecha;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;
}
