-- ============================================================
-- V7: Órdenes de Compra e Inventario
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================

-- ============================================================
-- Tabla: orden_compra
-- Descripción: Órdenes de compra generadas a partir de solicitudes
-- ============================================================
CREATE TABLE orden_compra (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    solicitud_id UUID NOT NULL,
    numero VARCHAR(20) UNIQUE,
    fecha DATE DEFAULT CURRENT_DATE,
    estado VARCHAR(20) DEFAULT 'pendiente',
    total DECIMAL(14,2) DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_orden_compra_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitud_servicio(id),
    CONSTRAINT fk_orden_compra_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE INDEX idx_orden_compra_solicitud_id ON orden_compra(solicitud_id);
CREATE INDEX idx_orden_compra_created_by ON orden_compra(created_by);
CREATE INDEX idx_orden_compra_estado ON orden_compra(estado);
CREATE INDEX idx_orden_compra_numero ON orden_compra(numero);

COMMENT ON TABLE orden_compra IS 'Órdenes de compra generadas para solicitudes de servicio';
COMMENT ON COLUMN orden_compra.estado IS 'Estado: pendiente, aprobada, enviada, recibida, cancelada';

-- ============================================================
-- Tabla: insumo
-- Descripción: Catálogo de insumos con control de inventario
-- ============================================================
CREATE TABLE insumo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    unidad_medida VARCHAR(20),
    stock_actual DECIMAL(10,2) DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

COMMENT ON TABLE insumo IS 'Catálogo de insumos con control de stock';
COMMENT ON COLUMN insumo.unidad_medida IS 'Unidad de medida: unidad, kg, litro, metro, etc.';
COMMENT ON COLUMN insumo.stock_actual IS 'Cantidad actual en inventario (actualizado por trigger)';

-- ============================================================
-- Tabla: insumo_movimiento
-- Descripción: Movimientos de inventario (ingresos y retiros)
-- ============================================================
CREATE TABLE insumo_movimiento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    insumo_id UUID NOT NULL,
    tipo_movimiento VARCHAR(10) NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    motivo TEXT,
    created_by UUID,

    CONSTRAINT fk_insumo_movimiento_insumo FOREIGN KEY (insumo_id) REFERENCES insumo(id),
    CONSTRAINT fk_insumo_movimiento_created_by FOREIGN KEY (created_by) REFERENCES usuario(id),
    CONSTRAINT chk_insumo_movimiento_tipo CHECK (tipo_movimiento IN ('ingreso', 'retiro')),
    CONSTRAINT chk_insumo_movimiento_cantidad CHECK (cantidad > 0)
);

CREATE INDEX idx_insumo_movimiento_insumo_id ON insumo_movimiento(insumo_id);
CREATE INDEX idx_insumo_movimiento_created_by ON insumo_movimiento(created_by);
CREATE INDEX idx_insumo_movimiento_tipo ON insumo_movimiento(tipo_movimiento);

COMMENT ON TABLE insumo_movimiento IS 'Registro de movimientos de inventario por insumo';
COMMENT ON COLUMN insumo_movimiento.tipo_movimiento IS 'Tipo: ingreso o retiro';
COMMENT ON COLUMN insumo_movimiento.motivo IS 'Razón del movimiento (ej: evento X, compra, ajuste)';

-- ============================================================
-- Agregar FK de evento_insumo a insumo (tabla definida en V6)
-- ============================================================
ALTER TABLE evento_insumo
    ADD CONSTRAINT fk_evento_insumo_insumo FOREIGN KEY (insumo_id) REFERENCES insumo(id);

-- ============================================================
-- Tabla: presentacion
-- Descripción: Presentaciones comerciales para clientes
-- ============================================================
CREATE TABLE presentacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    servicio_id UUID,
    activo BOOLEAN DEFAULT TRUE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_presentacion_servicio FOREIGN KEY (servicio_id) REFERENCES servicio(id),
    CONSTRAINT fk_presentacion_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE INDEX idx_presentacion_servicio_id ON presentacion(servicio_id);
CREATE INDEX idx_presentacion_created_by ON presentacion(created_by);

COMMENT ON TABLE presentacion IS 'Presentaciones comerciales asociadas a servicios';

-- ============================================================
-- Tabla: mensaje
-- Descripción: Plantillas de mensajes reutilizables
-- ============================================================
CREATE TABLE mensaje (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    contenido TEXT,
    activo BOOLEAN DEFAULT TRUE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_mensaje_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE INDEX idx_mensaje_created_by ON mensaje(created_by);

COMMENT ON TABLE mensaje IS 'Plantillas de mensajes reutilizables para comunicaciones';
