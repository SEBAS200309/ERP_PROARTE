package com.proarte.erp.servicios.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "servicio")
@SQLRestriction("activo = true")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Builder.Default
    @Column(name = "es_propio")
    private Boolean esPropio = true;

    @Builder.Default
    @Column(name = "requiere_oc")
    private Boolean requiereOc = false;

    @Column(name = "servicio_padre_id")
    private UUID servicioPadreId;

    @Column(name = "categoria_id")
    private UUID categoriaId;

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
