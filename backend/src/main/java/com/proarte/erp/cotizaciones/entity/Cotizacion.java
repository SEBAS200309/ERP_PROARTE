package com.proarte.erp.cotizaciones.entity;

import com.proarte.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "cotizacion")
@SQLRestriction("activo = true")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cotizacion extends BaseEntity {

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "estado_id", nullable = false)
    private UUID estadoId;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "total", precision = 14, scale = 2)
    private BigDecimal total;

    @Column(name = "persona_id")
    private UUID personaId;

    @Column(name = "empresa_id")
    private UUID empresaId;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id", insertable = false, updatable = false)
    @Builder.Default
    private List<CotizacionItem> items = new ArrayList<>();
}
