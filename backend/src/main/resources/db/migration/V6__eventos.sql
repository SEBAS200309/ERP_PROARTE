-- ============================================================
-- V6: Eventos (derivados de cotizaciones aprobadas)
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================

CREATE TABLE evento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cotizacion_id UUID NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    fecha_inicio TIMESTAMP WITH TIME ZONE,
    fecha_fin TIMESTAMP WITH TIME ZONE,
    lugar TEXT,
    estado_id UUID NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_evento_cotizacion FOREIGN KEY (cotizacion_id) REFERENCES cotizacion(id),
    CONSTRAINT fk_evento_estado FOREIGN KEY (estado_id) REFERENCES estado(id),
    CONSTRAINT fk_evento_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE INDEX idx_evento_cotizacion_id ON evento(cotizacion_id);
CREATE INDEX idx_evento_estado_id ON evento(estado_id);
CREATE INDEX idx_evento_created_by ON evento(created_by);
CREATE INDEX idx_evento_fecha_inicio ON evento(fecha_inicio);

COMMENT ON TABLE evento IS 'Eventos creados a partir de cotizaciones aprobadas';

CREATE TABLE evento_contacto (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    evento_id UUID NOT NULL,
    persona_id UUID NOT NULL,
    rol_evento_id UUID NOT NULL,
    observaciones TEXT,

    CONSTRAINT fk_evento_contacto_evento FOREIGN KEY (evento_id) REFERENCES evento(id),
    CONSTRAINT fk_evento_contacto_persona FOREIGN KEY (persona_id) REFERENCES persona(id),
    CONSTRAINT fk_evento_contacto_rol_evento FOREIGN KEY (rol_evento_id) REFERENCES rol_evento(id)
);

CREATE INDEX idx_evento_contacto_evento_id ON evento_contacto(evento_id);
CREATE INDEX idx_evento_contacto_persona_id ON evento_contacto(persona_id);
CREATE INDEX idx_evento_contacto_rol_evento_id ON evento_contacto(rol_evento_id);

COMMENT ON TABLE evento_contacto IS 'Personas de contacto asignadas a un evento';

CREATE TABLE evento_proveedor (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    evento_id UUID NOT NULL,
    proveedor_id UUID NOT NULL,
    servicio_id UUID,

    CONSTRAINT fk_evento_proveedor_evento FOREIGN KEY (evento_id) REFERENCES evento(id),
    CONSTRAINT fk_evento_proveedor_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedor(id),
    CONSTRAINT fk_evento_proveedor_servicio FOREIGN KEY (servicio_id) REFERENCES servicio(id)
);

CREATE INDEX idx_evento_proveedor_evento_id ON evento_proveedor(evento_id);
CREATE INDEX idx_evento_proveedor_proveedor_id ON evento_proveedor(proveedor_id);
CREATE INDEX idx_evento_proveedor_servicio_id ON evento_proveedor(servicio_id);

COMMENT ON TABLE evento_proveedor IS 'Proveedores asignados para prestar servicios en un evento';

CREATE TABLE evento_personal (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    evento_id UUID NOT NULL,
    persona_id UUID NOT NULL,
    proveedor_id UUID NOT NULL,
    servicio_id UUID,
    valor_turno DECIMAL(12,2) DEFAULT 0,
    tiene_arl BOOLEAN DEFAULT FALSE,
    tiene_op BOOLEAN DEFAULT FALSE,
    observaciones TEXT,

    CONSTRAINT fk_evento_personal_evento FOREIGN KEY (evento_id) REFERENCES evento(id),
    CONSTRAINT fk_evento_personal_persona FOREIGN KEY (persona_id) REFERENCES persona(id),
    CONSTRAINT fk_evento_personal_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedor(id),
    CONSTRAINT fk_evento_personal_servicio FOREIGN KEY (servicio_id) REFERENCES servicio(id)
);

CREATE INDEX idx_evento_personal_evento_id ON evento_personal(evento_id);
CREATE INDEX idx_evento_personal_persona_id ON evento_personal(persona_id);
CREATE INDEX idx_evento_personal_proveedor_id ON evento_personal(proveedor_id);
CREATE INDEX idx_evento_personal_servicio_id ON evento_personal(servicio_id);

COMMENT ON TABLE evento_personal IS 'Personal operativo asignado a turnos en eventos';

CREATE TABLE evento_observacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    evento_id UUID NOT NULL,
    texto TEXT NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_by UUID,

    CONSTRAINT fk_evento_observacion_evento FOREIGN KEY (evento_id) REFERENCES evento(id),
    CONSTRAINT fk_evento_observacion_created_by FOREIGN KEY (created_by) REFERENCES usuario(id)
);

CREATE INDEX idx_evento_observacion_evento_id ON evento_observacion(evento_id);
CREATE INDEX idx_evento_observacion_created_by ON evento_observacion(created_by);

COMMENT ON TABLE evento_observacion IS 'Bitácora de observaciones y notas por evento';

CREATE TABLE evento_insumo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    evento_id UUID NOT NULL,
    insumo_id UUID,
    cantidad DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_evento_insumo_evento FOREIGN KEY (evento_id) REFERENCES evento(id)
    -- FK a insumo se agrega en V7 cuando se cree la tabla insumo
);

CREATE INDEX idx_evento_insumo_evento_id ON evento_insumo(evento_id);
CREATE INDEX idx_evento_insumo_insumo_id ON evento_insumo(insumo_id);

COMMENT ON TABLE evento_insumo IS 'Insumos asignados a un evento';

CREATE TABLE evento_alimentacion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    evento_id UUID NOT NULL,
    descripcion VARCHAR(200),
    cantidad DECIMAL(10,2) NOT NULL,
    tipo_movimiento VARCHAR(10) NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_by UUID,

    CONSTRAINT fk_evento_alimentacion_evento FOREIGN KEY (evento_id) REFERENCES evento(id),
    CONSTRAINT fk_evento_alimentacion_created_by FOREIGN KEY (created_by) REFERENCES usuario(id),
    CONSTRAINT chk_evento_alimentacion_tipo CHECK (tipo_movimiento IN ('ingreso', 'retiro'))
);

CREATE INDEX idx_evento_alimentacion_evento_id ON evento_alimentacion(evento_id);
CREATE INDEX idx_evento_alimentacion_created_by ON evento_alimentacion(created_by);

COMMENT ON TABLE evento_alimentacion IS 'Control de movimientos de alimentación en eventos';

-- Agregar FK de solicitud_servicio a evento (definida en V3)
ALTER TABLE solicitud_servicio
    ADD CONSTRAINT fk_solicitud_evento FOREIGN KEY (evento_id) REFERENCES evento(id);
