-- ============================================================
-- V3: Proveedores y Servicios
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================

CREATE TABLE servicio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    categoria_id UUID,
    es_propio BOOLEAN DEFAULT TRUE NOT NULL,
    requiere_oc BOOLEAN DEFAULT FALSE NOT NULL,
    servicio_padre_id UUID,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_servicio_categoria FOREIGN KEY (categoria_id) REFERENCES categoria_servicio(id),
    CONSTRAINT fk_servicio_padre FOREIGN KEY (servicio_padre_id) REFERENCES servicio(id)
);

CREATE INDEX idx_servicio_padre_id ON servicio(servicio_padre_id);
CREATE INDEX idx_servicio_categoria_id ON servicio(categoria_id);

COMMENT ON TABLE servicio IS 'Catálogo de servicios ofrecidos por Pro Arte';
COMMENT ON COLUMN servicio.es_propio IS 'TRUE si el servicio es ejecutado directamente por Pro Arte';
COMMENT ON COLUMN servicio.requiere_oc IS 'TRUE si requiere orden de compra para su ejecución';

CREATE TABLE proveedor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    persona_id UUID,
    empresa_id UUID,
    especialidad VARCHAR(100),
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_proveedor_persona FOREIGN KEY (persona_id) REFERENCES persona(id),
    CONSTRAINT fk_proveedor_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_proveedor_created_by FOREIGN KEY (created_by) REFERENCES usuario(id),
    CONSTRAINT chk_proveedor_persona_o_empresa CHECK (persona_id IS NOT NULL OR empresa_id IS NOT NULL)
);

CREATE INDEX idx_proveedor_persona_id ON proveedor(persona_id);
CREATE INDEX idx_proveedor_empresa_id ON proveedor(empresa_id);
CREATE INDEX idx_proveedor_created_by ON proveedor(created_by);

COMMENT ON TABLE proveedor IS 'Proveedores de servicios vinculados a persona o empresa';

CREATE TABLE portafolio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proveedor_id UUID NOT NULL,
    servicio_id UUID NOT NULL,
    precio_unitario DECIMAL(12,2),
    activo BOOLEAN DEFAULT TRUE NOT NULL,

    CONSTRAINT fk_portafolio_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedor(id),
    CONSTRAINT fk_portafolio_servicio FOREIGN KEY (servicio_id) REFERENCES servicio(id),
    CONSTRAINT uq_portafolio_proveedor_servicio UNIQUE (proveedor_id, servicio_id)
);

CREATE INDEX idx_portafolio_proveedor_id ON portafolio(proveedor_id);
CREATE INDEX idx_portafolio_servicio_id ON portafolio(servicio_id);

COMMENT ON TABLE portafolio IS 'Catálogo de servicios por proveedor con precios unitarios';

CREATE TABLE solicitud_servicio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    proveedor_id UUID NOT NULL,
    servicio_id UUID NOT NULL,
    evento_id UUID,
    estado_id UUID NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_solicitud_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedor(id),
    CONSTRAINT fk_solicitud_servicio FOREIGN KEY (servicio_id) REFERENCES servicio(id),
    CONSTRAINT fk_solicitud_estado FOREIGN KEY (estado_id) REFERENCES estado(id),
    CONSTRAINT fk_solicitud_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
    -- FK a evento se agrega en V6 cuando se cree la tabla evento
);

CREATE INDEX idx_solicitud_proveedor_id ON solicitud_servicio(proveedor_id);
CREATE INDEX idx_solicitud_servicio_id ON solicitud_servicio(servicio_id);
CREATE INDEX idx_solicitud_evento_id ON solicitud_servicio(evento_id);
CREATE INDEX idx_solicitud_created_by ON solicitud_servicio(created_by);
CREATE INDEX idx_solicitud_estado_id ON solicitud_servicio(estado_id);

COMMENT ON TABLE solicitud_servicio IS 'Solicitudes de servicio enviadas a proveedores';
