-- ============================================================
-- V1: Esquema base — Extensiones, Lookup Tables y Seguridad
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================

-- Habilitar extensión para generación de UUIDs
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- TABLAS LOOKUP (Normalización)
-- ============================================================

CREATE TABLE tipo_documento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(20) NOT NULL UNIQUE
);

COMMENT ON TABLE tipo_documento IS 'Catálogo de tipos de documento de identidad (CC, CE, NIT, PA, etc.)';

CREATE TABLE rol_entidad (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL UNIQUE
);

COMMENT ON TABLE rol_entidad IS 'Catálogo de roles asignables a personas y empresas (contacto, cliente, proveedor, etc.)';

CREATE TABLE estado (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL,
    contexto VARCHAR(30) NOT NULL,
    CONSTRAINT uq_estado_nombre_contexto UNIQUE (nombre, contexto)
);

COMMENT ON TABLE estado IS 'Catálogo de estados parametrizados por contexto (lead, cotizacion, evento, etc.)';

CREATE TABLE categoria_servicio (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL UNIQUE
);

COMMENT ON TABLE categoria_servicio IS 'Catálogo de categorías de servicio (Propio, Tercero)';

CREATE TABLE unidad_medida (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(30) NOT NULL UNIQUE,
    abreviatura VARCHAR(10)
);

COMMENT ON TABLE unidad_medida IS 'Catálogo de unidades de medida para insumos (kg, lt, m, etc.)';

CREATE TABLE rol_evento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL UNIQUE
);

COMMENT ON TABLE rol_evento IS 'Catálogo de roles asignables a contactos dentro de un evento';

-- ============================================================
-- MÓDULO: Seguridad y Control de Acceso
-- ============================================================

CREATE TABLE rol (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

COMMENT ON TABLE rol IS 'Roles del sistema para control de acceso';
COMMENT ON COLUMN rol.nombre IS 'Nombre del rol (ej: Administrador, Comercial)';

CREATE TABLE permiso (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rol_id UUID NOT NULL,
    configuracion JSONB NOT NULL DEFAULT '{}',
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_permiso_rol FOREIGN KEY (rol_id) REFERENCES rol(id)
);

CREATE INDEX idx_permiso_rol_id ON permiso(rol_id);

COMMENT ON TABLE permiso IS 'Configuración de permisos por rol en formato JSONB';
COMMENT ON COLUMN permiso.configuracion IS 'Estructura JSON con permisos granulares por módulo';

CREATE TABLE usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    email VARCHAR(100),
    rol_id UUID NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,

    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol(id)
);

CREATE INDEX idx_usuario_rol_id ON usuario(rol_id);
CREATE INDEX idx_usuario_username ON usuario(username);

COMMENT ON TABLE usuario IS 'Usuarios del sistema con credenciales y rol asignado';
COMMENT ON COLUMN usuario.password_hash IS 'Hash BCrypt de la contraseña del usuario';


-- ============================================================
-- FKs de created_by (agregadas después de crear usuario)
-- ============================================================
ALTER TABLE rol ADD CONSTRAINT fk_rol_created_by FOREIGN KEY (created_by) REFERENCES usuario(id);
ALTER TABLE permiso ADD CONSTRAINT fk_permiso_created_by FOREIGN KEY (created_by) REFERENCES usuario(id);
