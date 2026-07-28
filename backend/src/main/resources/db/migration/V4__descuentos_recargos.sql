-- ============================================================
-- V4: Descuentos y Recargos (normalizado)
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================

-- ============================================================
-- Tabla: tipo_descuento_recargo
-- Descripción: Tipos de ajuste de precio (descuento o recargo)
-- ============================================================
CREATE TABLE tipo_descuento_recargo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL UNIQUE
);

COMMENT ON TABLE tipo_descuento_recargo IS 'Tipos de ajuste: Descuento o Recargo';

-- ============================================================
-- Tabla: descuento_recargo
-- Descripción: Definición de descuentos y recargos aplicables
-- ============================================================
CREATE TABLE descuento_recargo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    valor DECIMAL(5,2) NOT NULL,
    tipo_id UUID NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_descuento_recargo_tipo FOREIGN KEY (tipo_id) REFERENCES tipo_descuento_recargo(id),
    CONSTRAINT chk_descuento_recargo_valor CHECK (valor >= 0 AND valor <= 100)
);

CREATE INDEX idx_descuento_recargo_tipo_id ON descuento_recargo(tipo_id);

COMMENT ON TABLE descuento_recargo IS 'Descuentos y recargos aplicables a servicios, personas o empresas';
COMMENT ON COLUMN descuento_recargo.valor IS 'Porcentaje del descuento o recargo (0-100)';

-- ============================================================
-- Tabla: servicio_descuento_recargo
-- Descripción: Asignación de descuentos/recargos a servicios
-- ============================================================
CREATE TABLE servicio_descuento_recargo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    servicio_id UUID NOT NULL,
    descuento_recargo_id UUID NOT NULL,

    CONSTRAINT fk_sdr_servicio FOREIGN KEY (servicio_id) REFERENCES servicio(id),
    CONSTRAINT fk_sdr_descuento_recargo FOREIGN KEY (descuento_recargo_id) REFERENCES descuento_recargo(id),
    CONSTRAINT uq_servicio_descuento_recargo UNIQUE (servicio_id, descuento_recargo_id)
);

CREATE INDEX idx_sdr_servicio_id ON servicio_descuento_recargo(servicio_id);
CREATE INDEX idx_sdr_descuento_recargo_id ON servicio_descuento_recargo(descuento_recargo_id);

COMMENT ON TABLE servicio_descuento_recargo IS 'Descuentos/recargos asignados a servicios específicos';

-- ============================================================
-- Tabla: persona_descuento_recargo
-- Descripción: Asignación de descuentos/recargos a personas
-- ============================================================
CREATE TABLE persona_descuento_recargo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    persona_id UUID NOT NULL,
    descuento_recargo_id UUID NOT NULL,

    CONSTRAINT fk_pdr_persona FOREIGN KEY (persona_id) REFERENCES persona(id),
    CONSTRAINT fk_pdr_descuento_recargo FOREIGN KEY (descuento_recargo_id) REFERENCES descuento_recargo(id),
    CONSTRAINT uq_persona_descuento_recargo UNIQUE (persona_id, descuento_recargo_id)
);

CREATE INDEX idx_pdr_persona_id ON persona_descuento_recargo(persona_id);
CREATE INDEX idx_pdr_descuento_recargo_id ON persona_descuento_recargo(descuento_recargo_id);

COMMENT ON TABLE persona_descuento_recargo IS 'Descuentos/recargos asignados a personas específicas';

-- ============================================================
-- Tabla: empresa_descuento_recargo
-- Descripción: Asignación de descuentos/recargos a empresas
-- ============================================================
CREATE TABLE empresa_descuento_recargo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id UUID NOT NULL,
    descuento_recargo_id UUID NOT NULL,

    CONSTRAINT fk_edr_empresa FOREIGN KEY (empresa_id) REFERENCES empresa(id),
    CONSTRAINT fk_edr_descuento_recargo FOREIGN KEY (descuento_recargo_id) REFERENCES descuento_recargo(id),
    CONSTRAINT uq_empresa_descuento_recargo UNIQUE (empresa_id, descuento_recargo_id)
);

CREATE INDEX idx_edr_empresa_id ON empresa_descuento_recargo(empresa_id);
CREATE INDEX idx_edr_descuento_recargo_id ON empresa_descuento_recargo(descuento_recargo_id);

COMMENT ON TABLE empresa_descuento_recargo IS 'Descuentos/recargos asignados a empresas específicas';

-- ============================================================
-- Datos semilla: tipos de descuento/recargo
-- ============================================================
INSERT INTO tipo_descuento_recargo (id, nombre) VALUES
    (gen_random_uuid(), 'Descuento'),
    (gen_random_uuid(), 'Recargo');
