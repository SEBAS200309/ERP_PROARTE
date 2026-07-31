package com.proarte.erp.auth.entity;

import com.proarte.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "usuario")
@SQLRestriction("activo = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends BaseEntity {

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "rol_id", nullable = false)
    private UUID rolId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", insertable = false, updatable = false)
    private Rol rol;
}
