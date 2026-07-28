-- ============================================================
-- V2: Personas y Empresas — Módulo CRM
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================

-- ============================================================
-- Tabla: persona
-- Descripción: Personas naturales (contactos, clientes, artistas, etc.)
-- ============================================================
CREATE TABLE persona (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    tipo_documento VARCHAR(10),
    documento VARCHAR(20),
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion TEXT,
    rol_persona VARCHAR(20) DEFAULT 'contacto',
    activo BOOLEAN DEFAULT TRUE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_persona_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE INDEX idx_persona_created_by ON persona(created_by);
CREATE INDEX idx_persona_documento ON persona(documento);
CREATE INDEX idx_persona_rol ON persona(rol_persona);

COMMENT ON TABLE persona IS 'Personas naturales del sistema (contactos, clientes, artistas, proveedores)';
COMMENT ON COLUMN persona.rol_persona IS 'Rol de la persona: contacto, cliente, artista, proveedor';
COMMENT ON COLUMN persona.tipo_documento IS 'Tipo de documento: CC, CE, NIT, PA, etc.';

-- ============================================================
-- Tabla: empresa
-- Descripción: Empresas o personas jurídicas
-- ============================================================
CREATE TABLE empresa (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    razon_social VARCHAR(200) NOT NULL,
    nit VARCHAR(20),
    direccion TEXT,
    telefono VARCHAR(20),
    email VARCHAR(100),
    rol_empresa VARCHAR(20) DEFAULT 'cliente',
    activo BOOLEAN DEFAULT TRUE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_empresa_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE INDEX idx_empresa_created_by ON empresa(created_by);
CREATE INDEX idx_empresa_nit ON empresa(nit);
CREATE INDEX idx_empresa_rol ON empresa(rol_empresa);

COMMENT ON TABLE empresa IS 'Empresas o personas jurídicas del sistema';
COMMENT ON COLUMN empresa.rol_empresa IS 'Rol de la empresa: cliente, proveedor, aliado';

-- ============================================================
-- Tabla: persona_empresa
-- Descripción: Relación muchos-a-muchos entre personas y empresas
-- ============================================================
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

-- ============================================================
-- Tabla: lead
-- Descripción: Oportunidades comerciales (leads/prospectos)
-- ============================================================
CREATE TABLE lead (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    descripcion TEXT,
    estado VARCHAR(20) DEFAULT 'nuevo' NOT NULL,
    persona_id UUID,
    empresa_id UUID,
    activo BOOLEAN DEFAULT TRUE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_lead_persona FOREIGN KEY (persona_id) REFERENCES persona(id),
    CONSTRAINT fk_lead_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_lead_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE INDEX idx_lead_persona_id ON lead(persona_id);
CREATE INDEX idx_lead_empresa_id ON lead(empresa_id);
CREATE INDEX idx_lead_created_by ON lead(created_by);
CREATE INDEX idx_lead_estado ON lead(estado);

COMMENT ON TABLE lead IS 'Oportunidades comerciales y prospectos de venta';
COMMENT ON COLUMN lead.estado IS 'Estado del lead: nuevo, contactado, cotizado, ganado, perdido';
