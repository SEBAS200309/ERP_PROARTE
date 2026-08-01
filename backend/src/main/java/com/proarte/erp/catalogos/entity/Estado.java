package com.proarte.erp.catalogos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "estado", uniqueConstraints = {
        @UniqueConstraint(name = "uq_estado_nombre_contexto", columnNames = {"nombre", "contexto"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "contexto", nullable = false, length = 30)
    private String contexto;
}
