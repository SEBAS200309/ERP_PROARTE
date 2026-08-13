-- ============================================================
-- V2: Personas y Empresas — Módulo CRM
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================

CREATE TABLE persona (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    tipo_documento_id UUID,
    documento VARCHAR(20),
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    rol_entidad_id UUID,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_persona_tipo_documento FOREIGN KEY (tipo_documento_id) REFERENCES tipo_documento(id),
    CONSTRAINT fk_persona_rol_entidad FOREIGN KEY (rol_entidad_id) REFERENCES rol_entidad(id),
    CONSTRAINT fk_persona_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE INDEX idx_persona_created_by ON persona(created_by);
CREATE INDEX idx_persona_documento ON persona(documento);
CREATE INDEX idx_persona_tipo_documento_id ON persona(tipo_documento_id);
CREATE INDEX idx_persona_rol_entidad_id ON persona(rol_entidad_id);

COMMENT ON TABLE persona IS 'Personas naturales del sistema (contactos, clientes, artistas, proveedores)';

CREATE TABLE empresa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    razon_social VARCHAR(200) NOT NULL,
    nit VARCHAR(20),
    direccion TEXT,
    telefono VARCHAR(20),
    email VARCHAR(100),
    rol_entidad_id UUID,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_empresa_rol_entidad FOREIGN KEY (rol_entidad_id) REFERENCES rol_entidad(id),
    CONSTRAINT fk_empresa_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE INDEX idx_empresa_created_by ON empresa(created_by);
CREATE INDEX idx_empresa_nit ON empresa(nit);
CREATE INDEX idx_empresa_rol_entidad_id ON empresa(rol_entidad_id);

COMMENT ON TABLE empresa IS 'Empresas o personas jurídicas del sistema';

CREATE TABLE persona_empresa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    persona_id UUID NOT NULL,
    empresa_id UUID NOT NULL,
    cargo VARCHAR(100),

    CONSTRAINT fk_persona_empresa_persona FOREIGN KEY (persona_id) REFERENCES persona(id),
    CONSTRAINT fk_persona_empresa_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT uq_persona_empresa UNIQUE (persona_id, empresa_id)
);

CREATE INDEX idx_persona_empresa_persona_id ON persona_empresa(persona_id);
CREATE INDEX idx_persona_empresa_empresa_id ON persona_empresa(empresa_id);

COMMENT ON TABLE persona_empresa IS 'Vinculación de personas con empresas y su cargo';

CREATE TABLE lead (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    descripcion TEXT,
    estado_id UUID NOT NULL,
    persona_id UUID,
    empresa_id UUID,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_lead_estado FOREIGN KEY (estado_id) REFERENCES estado(id),
    CONSTRAINT fk_lead_persona FOREIGN KEY (persona_id) REFERENCES persona(id),
    CONSTRAINT fk_lead_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_lead_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE INDEX idx_lead_persona_id ON lead(persona_id);
CREATE INDEX idx_lead_empresa_id ON lead(empresa_id);
CREATE INDEX idx_lead_created_by ON lead(created_by);
CREATE INDEX idx_lead_estado_id ON lead(estado_id);

COMMENT ON TABLE lead IS 'Oportunidades comerciales y prospectos de venta';
