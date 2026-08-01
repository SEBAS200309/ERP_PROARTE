package com.proarte.erp.servicios.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "descuento_recargo")
@SQLRestriction("activo = true")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DescuentoRecargo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "valor", nullable = false, precision = 5, scale = 2)
    private BigDecimal valor;

    @Column(name = "tipo_id", nullable = false)
    private UUID tipoId;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
