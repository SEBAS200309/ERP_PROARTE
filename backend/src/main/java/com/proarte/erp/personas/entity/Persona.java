package com.proarte.erp.personas.entity;

import com.proarte.erp.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "persona")
@SQLRestriction("activo = true")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Persona extends BaseEntity {

    @Column(name = "nombres", length = 100, nullable = false)
    private String nombres;

    @Column(name = "apellidos", length = 100, nullable = false)
    private String apellidos;

    @Column(name = "tipo_documento", length = 10)
    private String tipoDocumento;

    @Column(name = "documento", length = 20)
    private String documento;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "rol_persona", length = 20)
    private String rolPersona;
}
