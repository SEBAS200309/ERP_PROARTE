package com.proarte.erp.catalogos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "unidad_medida")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnidadMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 30, unique = true)
    private String nombre;

    @Column(name = "abreviatura", length = 10)
    private String abreviatura;
}
