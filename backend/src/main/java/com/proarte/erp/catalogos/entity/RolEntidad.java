package com.proarte.erp.catalogos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "rol_entidad")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;
}
