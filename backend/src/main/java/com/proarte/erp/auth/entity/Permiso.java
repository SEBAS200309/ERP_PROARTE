package com.proarte.erp.auth.entity;

import com.proarte.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "permiso")
@SQLRestriction("activo = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permiso extends BaseEntity {

    @Column(name = "rol_id", nullable = false)
    private UUID rolId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuracion", nullable = false, columnDefinition = "jsonb")
    private Map<String, Map<String, Boolean>> configuracion;
}
