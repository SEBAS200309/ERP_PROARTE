-- ============================================================
-- V1: Esquema base — Extensiones y tablas de seguridad
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================

-- Habilitar extensión para generación de UUIDs
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- Tabla: rol
-- Descripción: Roles del sistema para control de acceso
-- ============================================================
CREATE TABLE rol (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(50) NOT NULL,
    descripcion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

COMMENT ON TABLE rol IS 'Roles del sistema para control de acceso';
COMMENT ON COLUMN rol.nombre IS 'Nombre del rol (ej: Administrador, Comercial)';

-- ============================================================
-- Tabla: permiso
-- Descripción: Permisos asociados a un rol, almacenados en JSONB
-- ============================================================
CREATE TABLE permiso (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rol_id UUID NOT NULL,
    configuracion JSONB NOT NULL DEFAULT '{}',
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_permiso_rol FOREIGN KEY (rol_id) REFERENCES rol(id)
);

CREATE INDEX idx_permiso_rol_id ON permiso(rol_id);

COMMENT ON TABLE permiso IS 'Configuración de permisos por rol en formato JSONB';
COMMENT ON COLUMN permiso.configuracion IS 'Estructura JSON con permisos granulares por módulo';

-- ============================================================
-- Tabla: usuario
-- Descripción: Usuarios del sistema con autenticación local
-- ============================================================
CREATE TABLE usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(150) NOT NULL,
    email VARCHAR(100),
    rol_id UUID NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol(id)
);

CREATE INDEX idx_usuario_rol_id ON usuario(rol_id);
CREATE INDEX idx_usuario_username ON usuario(username);

COMMENT ON TABLE usuario IS 'Usuarios del sistema con credenciales y rol asignado';
COMMENT ON COLUMN usuario.password_hash IS 'Hash BCrypt de la contraseña del usuario';
