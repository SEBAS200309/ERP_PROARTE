-- ============================================================
-- V10: Normalización — Tablas de Lookup (Catálogos)
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================
-- Esta migración normaliza las columnas VARCHAR que funcionan
-- como enums en texto plano, reemplazándolas por FK a tablas
-- de catálogo dedicadas. Esto mejora la integridad referencial,
-- facilita la gestión desde la UI y permite auditoría.
-- ============================================================

-- ============================================================
-- PASO 1: Crear tablas de lookup (catálogos)
-- ============================================================

-- ============================================================
-- Tabla: tipo_documento
-- Descripción: Tipos de documento de identidad válidos
-- ============================================================
CREATE TABLE tipo_documento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(20) NOT NULL UNIQUE
);

COMMENT ON TABLE tipo_documento IS 'Catálogo de tipos de documento de identidad (CC, CE, NIT, PA, etc.)';

-- ============================================================
-- Tabla: rol_entidad
-- Descripción: Roles compartidos entre persona y empresa
-- ============================================================
CREATE TABLE rol_entidad (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL UNIQUE
);

COMMENT ON TABLE rol_entidad IS 'Catálogo de roles asignables a personas y empresas (contacto, cliente, proveedor, etc.)';

-- ============================================================
-- Tabla: estado
-- Descripción: Estados parametrizados por contexto de negocio
-- ============================================================
CREATE TABLE estado (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL,
    contexto VARCHAR(30) NOT NULL,
    CONSTRAINT uq_estado_nombre_contexto UNIQUE (nombre, contexto)
);

COMMENT ON TABLE estado IS 'Catálogo de estados parametrizados por contexto (lead, cotizacion, evento, etc.)';

-- ============================================================
-- Tabla: categoria_servicio
-- Descripción: Categorías de servicios (Propio, Tercero)
-- ============================================================
CREATE TABLE categoria_servicio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL UNIQUE
);

COMMENT ON TABLE categoria_servicio IS 'Catálogo de categorías de servicio (Propio, Tercero)';

-- ============================================================
-- Tabla: unidad_medida
-- Descripción: Unidades de medida para insumos
-- ============================================================
CREATE TABLE unidad_medida (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(30) NOT NULL UNIQUE,
    abreviatura VARCHAR(10)
);

COMMENT ON TABLE unidad_medida IS 'Catálogo de unidades de medida para insumos (kg, lt, m, etc.)';

-- ============================================================
-- Tabla: rol_evento
-- Descripción: Roles que puede tener un contacto en un evento
-- ============================================================
CREATE TABLE rol_evento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL UNIQUE
);

COMMENT ON TABLE rol_evento IS 'Catálogo de roles asignables a contactos dentro de un evento';

-- ============================================================
-- PASO 2: Poblar datos iniciales (seed)
-- ============================================================

-- tipo_documento
INSERT INTO tipo_documento (nombre) VALUES ('CC'), ('CE'), ('NIT'), ('PA'), ('TI'), ('RC');

-- rol_entidad (compartido entre persona y empresa)
INSERT INTO rol_entidad (nombre) VALUES ('contacto'), ('cliente'), ('proveedor'), ('aliado'), ('artista');

-- estado con contexto
INSERT INTO estado (nombre, contexto) VALUES
  ('nuevo', 'lead'), ('contactado', 'lead'), ('cotizado', 'lead'), ('ganado', 'lead'), ('perdido', 'lead'),
  ('borrador', 'cotizacion'), ('enviada', 'cotizacion'), ('aprobada', 'cotizacion'), ('rechazada', 'cotizacion'), ('vencida', 'cotizacion'),
  ('planificacion', 'evento'), ('en_curso', 'evento'), ('finalizado', 'evento'), ('cancelado', 'evento'),
  ('pendiente', 'solicitud_servicio'), ('aceptada', 'solicitud_servicio'), ('rechazada', 'solicitud_servicio'), ('completada', 'solicitud_servicio'),
  ('pendiente', 'orden_compra'), ('aprobada', 'orden_compra'), ('enviada', 'orden_compra'), ('recibida', 'orden_compra'), ('cancelada', 'orden_compra');

-- categoria_servicio
INSERT INTO categoria_servicio (nombre) VALUES ('Propio'), ('Tercero');

-- unidad_medida
INSERT INTO unidad_medida (nombre, abreviatura) VALUES
  ('Unidad', 'ud'), ('Kilogramo', 'kg'), ('Litro', 'lt'), ('Metro', 'm'), ('Caja', 'cj');

-- rol_evento
INSERT INTO rol_evento (nombre) VALUES ('organizador'), ('responsable'), ('asistente'), ('promotor'), ('coordinador'), ('personal');

-- ============================================================
-- PASO 3: Agregar columnas FK nuevas a tablas existentes
-- ============================================================

-- persona
ALTER TABLE persona ADD COLUMN tipo_documento_id UUID;
ALTER TABLE persona ADD COLUMN rol_entidad_id UUID;

-- empresa
ALTER TABLE empresa ADD COLUMN rol_entidad_id UUID;

-- lead
ALTER TABLE lead ADD COLUMN estado_id UUID;

-- servicio
ALTER TABLE servicio ADD COLUMN categoria_id UUID;

-- solicitud_servicio
ALTER TABLE solicitud_servicio ADD COLUMN estado_id UUID;

-- cotizacion
ALTER TABLE cotizacion ADD COLUMN estado_id UUID;

-- evento
ALTER TABLE evento ADD COLUMN estado_id UUID;

-- evento_contacto
ALTER TABLE evento_contacto ADD COLUMN rol_evento_id UUID;

-- orden_compra
ALTER TABLE orden_compra ADD COLUMN estado_id UUID;

-- insumo
ALTER TABLE insumo ADD COLUMN unidad_medida_id UUID;

-- ============================================================
-- PASO 4: Migrar datos existentes (UPDATE con subquery)
-- ============================================================

-- persona.tipo_documento → tipo_documento_id
UPDATE persona SET tipo_documento_id = (SELECT id FROM tipo_documento WHERE nombre = persona.tipo_documento)
WHERE tipo_documento IS NOT NULL;

-- persona.rol_persona → rol_entidad_id
UPDATE persona SET rol_entidad_id = (SELECT id FROM rol_entidad WHERE nombre = persona.rol_persona)
WHERE rol_persona IS NOT NULL;

-- empresa.rol_empresa → rol_entidad_id
UPDATE empresa SET rol_entidad_id = (SELECT id FROM rol_entidad WHERE nombre = empresa.rol_empresa)
WHERE rol_empresa IS NOT NULL;

-- lead.estado → estado_id
UPDATE lead SET estado_id = (SELECT id FROM estado WHERE nombre = lead.estado AND contexto = 'lead')
WHERE estado IS NOT NULL;

-- servicio.categoria → categoria_id
UPDATE servicio SET categoria_id = (SELECT id FROM categoria_servicio WHERE LOWER(nombre) = LOWER(servicio.categoria))
WHERE categoria IS NOT NULL;

-- solicitud_servicio.estado → estado_id
UPDATE solicitud_servicio SET estado_id = (SELECT id FROM estado WHERE nombre = solicitud_servicio.estado AND contexto = 'solicitud_servicio')
WHERE estado IS NOT NULL;

-- cotizacion.estado → estado_id
UPDATE cotizacion SET estado_id = (SELECT id FROM estado WHERE nombre = cotizacion.estado AND contexto = 'cotizacion')
WHERE estado IS NOT NULL;

-- evento.estado → estado_id
UPDATE evento SET estado_id = (SELECT id FROM estado WHERE nombre = evento.estado AND contexto = 'evento')
WHERE estado IS NOT NULL;

-- evento_contacto.rol_evento → rol_evento_id
UPDATE evento_contacto SET rol_evento_id = (SELECT id FROM rol_evento WHERE nombre = evento_contacto.rol_evento)
WHERE rol_evento IS NOT NULL;

-- orden_compra.estado → estado_id
UPDATE orden_compra SET estado_id = (SELECT id FROM estado WHERE nombre = orden_compra.estado AND contexto = 'orden_compra')
WHERE estado IS NOT NULL;

-- insumo.unidad_medida → unidad_medida_id
UPDATE insumo SET unidad_medida_id = (SELECT id FROM unidad_medida WHERE LOWER(nombre) = LOWER(insumo.unidad_medida))
WHERE unidad_medida IS NOT NULL;

-- ============================================================
-- PASO 5: Establecer NOT NULL donde corresponde
-- ============================================================

-- Solo se aplica NOT NULL en columnas que eran NOT NULL antes de la migración
ALTER TABLE lead ALTER COLUMN estado_id SET NOT NULL;
ALTER TABLE cotizacion ALTER COLUMN estado_id SET NOT NULL;

-- ============================================================
-- PASO 6: Agregar constraints de FK
-- ============================================================

ALTER TABLE persona ADD CONSTRAINT fk_persona_tipo_documento FOREIGN KEY (tipo_documento_id) REFERENCES tipo_documento(id);
ALTER TABLE persona ADD CONSTRAINT fk_persona_rol_entidad FOREIGN KEY (rol_entidad_id) REFERENCES rol_entidad(id);
ALTER TABLE empresa ADD CONSTRAINT fk_empresa_rol_entidad FOREIGN KEY (rol_entidad_id) REFERENCES rol_entidad(id);
ALTER TABLE lead ADD CONSTRAINT fk_lead_estado FOREIGN KEY (estado_id) REFERENCES estado(id);
ALTER TABLE servicio ADD CONSTRAINT fk_servicio_categoria FOREIGN KEY (categoria_id) REFERENCES categoria_servicio(id);
ALTER TABLE solicitud_servicio ADD CONSTRAINT fk_solicitud_estado FOREIGN KEY (estado_id) REFERENCES estado(id);
ALTER TABLE cotizacion ADD CONSTRAINT fk_cotizacion_estado FOREIGN KEY (estado_id) REFERENCES estado(id);
ALTER TABLE evento ADD CONSTRAINT fk_evento_estado FOREIGN KEY (estado_id) REFERENCES estado(id);
ALTER TABLE evento_contacto ADD CONSTRAINT fk_evento_contacto_rol_evento FOREIGN KEY (rol_evento_id) REFERENCES rol_evento(id);
ALTER TABLE orden_compra ADD CONSTRAINT fk_orden_compra_estado FOREIGN KEY (estado_id) REFERENCES estado(id);
ALTER TABLE insumo ADD CONSTRAINT fk_insumo_unidad_medida FOREIGN KEY (unidad_medida_id) REFERENCES unidad_medida(id);

-- ============================================================
-- PASO 7: Crear índices en columnas FK nuevas
-- ============================================================

CREATE INDEX idx_persona_tipo_documento_id ON persona(tipo_documento_id);
CREATE INDEX idx_persona_rol_entidad_id ON persona(rol_entidad_id);
CREATE INDEX idx_empresa_rol_entidad_id ON empresa(rol_entidad_id);
CREATE INDEX idx_lead_estado_id ON lead(estado_id);
CREATE INDEX idx_servicio_categoria_id ON servicio(categoria_id);
CREATE INDEX idx_solicitud_estado_id ON solicitud_servicio(estado_id);
CREATE INDEX idx_cotizacion_estado_id ON cotizacion(estado_id);
CREATE INDEX idx_evento_estado_id ON evento(estado_id);
CREATE INDEX idx_evento_contacto_rol_evento_id ON evento_contacto(rol_evento_id);
CREATE INDEX idx_orden_compra_estado_id ON orden_compra(estado_id);
CREATE INDEX idx_insumo_unidad_medida_id ON insumo(unidad_medida_id);

-- ============================================================
-- PASO 8: Eliminar columnas VARCHAR e índices obsoletos
-- ============================================================

-- Eliminar índices antiguos
DROP INDEX IF EXISTS idx_persona_rol;
DROP INDEX IF EXISTS idx_empresa_rol;
DROP INDEX IF EXISTS idx_servicio_categoria;
DROP INDEX IF EXISTS idx_solicitud_estado;
DROP INDEX IF EXISTS idx_cotizacion_estado;
DROP INDEX IF EXISTS idx_evento_estado;
DROP INDEX IF EXISTS idx_orden_compra_estado;
DROP INDEX IF EXISTS idx_lead_estado;

-- Eliminar columnas VARCHAR obsoletas
ALTER TABLE persona DROP COLUMN tipo_documento;
ALTER TABLE persona DROP COLUMN rol_persona;
ALTER TABLE empresa DROP COLUMN rol_empresa;
ALTER TABLE lead DROP COLUMN estado;
ALTER TABLE servicio DROP COLUMN categoria;
ALTER TABLE solicitud_servicio DROP COLUMN estado;
ALTER TABLE cotizacion DROP COLUMN estado;
ALTER TABLE evento DROP COLUMN estado;
ALTER TABLE evento_contacto DROP COLUMN rol_evento;
ALTER TABLE orden_compra DROP COLUMN estado;
ALTER TABLE insumo DROP COLUMN unidad_medida;

-- ============================================================
-- PASO 9: Actualizar funciones de V8 para usar JOINs con estado
-- ============================================================

-- ============================================================
-- Función: fn_crear_evento_desde_cotizacion (actualizada)
-- Descripción: Crea un evento a partir de una cotización aprobada
--              Ahora usa JOIN con tabla estado en vez de VARCHAR
-- ============================================================
CREATE OR REPLACE FUNCTION fn_crear_evento_desde_cotizacion(p_cotizacion_id UUID)
RETURNS UUID
LANGUAGE plpgsql
AS $$
DECLARE
    v_cotizacion RECORD;
    v_evento_id UUID;
    v_estado_nombre VARCHAR;
BEGIN
    SELECT c.id, c.codigo, e.nombre AS estado_nombre
    INTO v_cotizacion
    FROM cotizacion c
    JOIN estado e ON e.id = c.estado_id
    WHERE c.id = p_cotizacion_id;

    IF v_cotizacion.estado_nombre != 'aprobada' THEN
        RAISE EXCEPTION 'La cotización debe estar aprobada para crear un evento. Estado actual: %', v_cotizacion.estado_nombre;
    END IF;

    INSERT INTO evento (cotizacion_id, nombre, estado_id)
    VALUES (p_cotizacion_id, 'Evento - ' || v_cotizacion.codigo,
            (SELECT id FROM estado WHERE nombre = 'planificacion' AND contexto = 'evento'))
    RETURNING id INTO v_evento_id;

    RETURN v_evento_id;
END;
$$;

COMMENT ON FUNCTION fn_crear_evento_desde_cotizacion(UUID) IS 'Crea un evento nuevo a partir de una cotización aprobada (normalizado con tabla estado)';

-- ============================================================
-- Función: fn_cotizaciones_por_vencer (actualizada)
-- Descripción: Retorna cotizaciones próximas a vencer en N días
--              Ahora usa JOIN con tabla estado en vez de VARCHAR
-- ============================================================
CREATE OR REPLACE FUNCTION fn_cotizaciones_por_vencer(p_dias INTEGER DEFAULT 7)
RETURNS SETOF cotizacion
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT c.*
    FROM cotizacion c
    JOIN estado e ON e.id = c.estado_id
    WHERE c.fecha_vencimiento BETWEEN CURRENT_DATE AND CURRENT_DATE + p_dias
      AND e.nombre NOT IN ('aprobada', 'rechazada', 'vencida')
      AND c.activo = TRUE
    ORDER BY c.fecha_vencimiento ASC;
END;
$$;

COMMENT ON FUNCTION fn_cotizaciones_por_vencer(INTEGER) IS 'Retorna cotizaciones que vencen dentro de los próximos N días (normalizado con tabla estado)';

-- ============================================================
-- Función: fn_estadisticas_leads (actualizada)
-- Descripción: Retorna estadísticas de leads agrupados por estado
--              Ahora usa JOIN con tabla estado en vez de VARCHAR
-- ============================================================
CREATE OR REPLACE FUNCTION fn_estadisticas_leads()
RETURNS TABLE(estado VARCHAR, cantidad BIGINT)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT e.nombre::VARCHAR AS estado, COUNT(*)::BIGINT AS cantidad
    FROM lead l
    JOIN estado e ON e.id = l.estado_id
    WHERE l.activo = TRUE
    GROUP BY e.nombre
    ORDER BY cantidad DESC;
END;
$$;

COMMENT ON FUNCTION fn_estadisticas_leads() IS 'Retorna conteo de leads agrupados por estado (normalizado con tabla estado)';
