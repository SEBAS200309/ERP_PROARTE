package com.proarte.erp.empresas.entity;

import com.proarte.erp.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "empresa")
@SQLRestriction("activo = true")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Empresa extends BaseEntity {

    @Column(name = "razon_social", length = 200, nullable = false)
    private String razonSocial;

    @Column(name = "nit", length = 20)
    private String nit;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "rol_empresa", length = 20)
    private String rolEmpresa;
}
