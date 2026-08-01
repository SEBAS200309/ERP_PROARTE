package com.proarte.erp.personas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "persona_empresa")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonaEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "persona_id", nullable = false)
    private UUID personaId;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "cargo", length = 100)
    private String cargo;
}
