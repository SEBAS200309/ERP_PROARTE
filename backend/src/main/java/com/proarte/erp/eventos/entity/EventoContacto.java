package com.proarte.erp.eventos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "evento_contacto")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "evento_id", nullable = false)
    private UUID eventoId;

    @Column(name = "persona_id", nullable = false)
    private UUID personaId;

    @Column(name = "rol_evento_id")
    private UUID rolEventoId;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
