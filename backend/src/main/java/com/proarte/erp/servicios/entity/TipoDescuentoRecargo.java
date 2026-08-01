package com.proarte.erp.servicios.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tipo_descuento_recargo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoDescuentoRecargo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;
}
