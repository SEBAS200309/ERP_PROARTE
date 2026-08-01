package com.proarte.erp.eventos.entity;

import com.proarte.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "evento")
@SQLRestriction("activo = true")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Evento extends BaseEntity {

    @Column(name = "cotizacion_id", nullable = false)
    private UUID cotizacionId;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "fecha_inicio")
    private OffsetDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private OffsetDateTime fechaFin;

    @Column(name = "lugar", columnDefinition = "TEXT")
    private String lugar;

    @Column(name = "estado_id")
    private UUID estadoId;
}
