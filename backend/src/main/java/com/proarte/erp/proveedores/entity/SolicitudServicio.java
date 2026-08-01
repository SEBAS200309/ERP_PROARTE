package com.proarte.erp.proveedores.entity;

import com.proarte.erp.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "solicitud_servicio")
@SQLRestriction("activo = true")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudServicio extends BaseEntity {

    @Column(name = "proveedor_id", nullable = false)
    private UUID proveedorId;

    @Column(name = "servicio_id", nullable = false)
    private UUID servicioId;

    @Column(name = "evento_id")
    private UUID eventoId;

    @Column(name = "estado_id")
    private UUID estadoId;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
}
