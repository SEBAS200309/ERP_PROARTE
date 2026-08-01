package com.proarte.erp.inventario.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "insumo_movimiento")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsumoMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "insumo_id", nullable = false)
    private UUID insumoId;

    @Column(name = "tipo_movimiento", nullable = false, length = 10)
    private String tipoMovimiento;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "fecha")
    private OffsetDateTime fecha;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;
}
