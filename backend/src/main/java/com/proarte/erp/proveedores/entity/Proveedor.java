package com.proarte.erp.proveedores.entity;

import com.proarte.erp.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "proveedor")
@SQLRestriction("activo = true")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor extends BaseEntity {

    @Column(name = "persona_id")
    private UUID personaId;

    @Column(name = "empresa_id")
    private UUID empresaId;

    @Column(name = "especialidad", length = 100)
    private String especialidad;
}
