-- ============================================================
-- V5: Cotizaciones
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================

CREATE TABLE cotizacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo VARCHAR(20) UNIQUE NOT NULL,
    estado_id UUID NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fecha_vencimiento DATE,
    total DECIMAL(14,2) DEFAULT 0,
    persona_id UUID,
    empresa_id UUID,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    created_by UUID,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_cotizacion_estado FOREIGN KEY (estado_id) REFERENCES estado(id),
    CONSTRAINT fk_cotizacion_persona FOREIGN KEY (persona_id) REFERENCES persona(id),
    CONSTRAINT fk_cotizacion_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_cotizacion_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE INDEX idx_cotizacion_persona_id ON cotizacion(persona_id);
CREATE INDEX idx_cotizacion_empresa_id ON cotizacion(empresa_id);
CREATE INDEX idx_cotizacion_created_by ON cotizacion(created_by);
CREATE INDEX idx_cotizacion_estado_id ON cotizacion(estado_id);
CREATE INDEX idx_cotizacion_codigo ON cotizacion(codigo);
CREATE INDEX idx_cotizacion_fecha_vencimiento ON cotizacion(fecha_vencimiento);

COMMENT ON TABLE cotizacion IS 'Cotizaciones comerciales enviadas a clientes';
COMMENT ON COLUMN cotizacion.codigo IS 'Código único de la cotización (ej: COT-2024-001)';
COMMENT ON COLUMN cotizacion.total IS 'Total calculado automáticamente desde los ítems';

CREATE TABLE cotizacion_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cotizacion_id UUID NOT NULL,
    servicio_id UUID NOT NULL,
    cantidad INTEGER DEFAULT 1 NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    descuento_recargo_id UUID,
    subtotal DECIMAL(14,2) DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_cotizacion_item_cotizacion FOREIGN KEY (cotizacion_id) REFERENCES cotizacion(id),
    CONSTRAINT fk_cotizacion_item_servicio FOREIGN KEY (servicio_id) REFERENCES servicio(id),
    CONSTRAINT fk_cotizacion_item_descuento_recargo FOREIGN KEY (descuento_recargo_id) REFERENCES descuento_recargo(id)
);

CREATE INDEX idx_cotizacion_item_cotizacion_id ON cotizacion_item(cotizacion_id);
CREATE INDEX idx_cotizacion_item_servicio_id ON cotizacion_item(servicio_id);
CREATE INDEX idx_cotizacion_item_descuento_recargo_id ON cotizacion_item(descuento_recargo_id);

COMMENT ON TABLE cotizacion_item IS 'Ítems individuales dentro de una cotización';
COMMENT ON COLUMN cotizacion_item.subtotal IS 'Subtotal calculado: cantidad * precio_unitario ± descuento/recargo';
