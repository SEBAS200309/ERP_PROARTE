-- ============================================================
-- V9: Datos semilla — Lookups, Roles, Permisos y Admin
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================

-- ============================================================
-- Datos de tablas Lookup
-- ============================================================

INSERT INTO tipo_documento (nombre) VALUES ('CC'), ('CE'), ('NIT'), ('PA'), ('TI'), ('RC');

INSERT INTO rol_entidad (nombre) VALUES ('contacto'), ('cliente'), ('proveedor'), ('aliado'), ('artista');

INSERT INTO estado (nombre, contexto) VALUES
  ('nuevo', 'lead'), ('contactado', 'lead'), ('cotizado', 'lead'), ('ganado', 'lead'), ('perdido', 'lead'),
  ('borrador', 'cotizacion'), ('enviada', 'cotizacion'), ('aprobada', 'cotizacion'), ('rechazada', 'cotizacion'), ('vencida', 'cotizacion'),
  ('planificacion', 'evento'), ('en_curso', 'evento'), ('finalizado', 'evento'), ('cancelado', 'evento'),
  ('pendiente', 'solicitud_servicio'), ('aceptada', 'solicitud_servicio'), ('rechazada', 'solicitud_servicio'), ('completada', 'solicitud_servicio'),
  ('pendiente', 'orden_compra'), ('aprobada', 'orden_compra'), ('enviada', 'orden_compra'), ('recibida', 'orden_compra'), ('cancelada', 'orden_compra');

INSERT INTO categoria_servicio (nombre) VALUES ('Propio'), ('Tercero');

INSERT INTO unidad_medida (nombre, abreviatura) VALUES
  ('Unidad', 'ud'), ('Kilogramo', 'kg'), ('Litro', 'lt'), ('Metro', 'm'), ('Caja', 'cj');

INSERT INTO rol_evento (nombre) VALUES ('organizador'), ('responsable'), ('asistente'), ('promotor'), ('coordinador'), ('personal');

-- ============================================================
-- Roles del sistema
-- ============================================================
INSERT INTO rol (id, nombre, descripcion) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Administrador', 'Acceso total al sistema'),
    ('a0000000-0000-0000-0000-000000000002', 'Comercial', 'Gestión comercial'),
    ('a0000000-0000-0000-0000-000000000003', 'Operativo', 'Gestión operativa de eventos'),
    ('a0000000-0000-0000-0000-000000000004', 'Coordinador', 'Consulta de eventos asignados');

-- ============================================================
-- Permisos del rol Administrador — acceso total
-- ============================================================
INSERT INTO permiso (rol_id, configuracion) VALUES
('a0000000-0000-0000-0000-000000000001', '{
    "usuarios": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "roles": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "personas": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "empresas": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "leads": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "servicios": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "proveedores": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "cotizaciones": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "eventos": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "personal": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "ordenes": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "inventario": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "alimentacion": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "presentaciones": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "mensajes": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "observaciones": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "reportes": {"crear": true, "leer": true, "editar": true, "eliminar": true}
}'::jsonb);

-- ============================================================
-- Permisos del rol Comercial
-- ============================================================
INSERT INTO permiso (rol_id, configuracion) VALUES
('a0000000-0000-0000-0000-000000000002', '{
    "usuarios": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "roles": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "personas": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "empresas": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "leads": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "servicios": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "proveedores": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "cotizaciones": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "eventos": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "personal": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "ordenes": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "inventario": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "alimentacion": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "presentaciones": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "mensajes": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "observaciones": {"crear": true, "leer": true, "editar": false, "eliminar": false},
    "reportes": {"crear": false, "leer": true, "editar": false, "eliminar": false}
}'::jsonb);

-- ============================================================
-- Permisos del rol Operativo
-- ============================================================
INSERT INTO permiso (rol_id, configuracion) VALUES
('a0000000-0000-0000-0000-000000000003', '{
    "usuarios": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "roles": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "personas": {"crear": true, "leer": true, "editar": true, "eliminar": false},
    "empresas": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "leads": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "servicios": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "proveedores": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "cotizaciones": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "eventos": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "personal": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "ordenes": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "inventario": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "alimentacion": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "presentaciones": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "mensajes": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "observaciones": {"crear": true, "leer": true, "editar": true, "eliminar": false},
    "reportes": {"crear": false, "leer": true, "editar": false, "eliminar": false}
}'::jsonb);

-- ============================================================
-- Permisos del rol Coordinador — solo lectura limitada
-- ============================================================
INSERT INTO permiso (rol_id, configuracion) VALUES
('a0000000-0000-0000-0000-000000000004', '{
    "usuarios": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "roles": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "personas": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "empresas": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "leads": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "servicios": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "proveedores": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "cotizaciones": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "eventos": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "personal": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "ordenes": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "inventario": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "alimentacion": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "presentaciones": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "mensajes": {"crear": false, "leer": false, "editar": false, "eliminar": false},
    "observaciones": {"crear": false, "leer": true, "editar": false, "eliminar": false},
    "reportes": {"crear": false, "leer": false, "editar": false, "eliminar": false}
}'::jsonb);

-- ============================================================
-- Usuario administrador por defecto
-- Password: ProArte2024! (hash BCrypt)
-- ============================================================
INSERT INTO usuario (id, username, password_hash, nombre_completo, email, rol_id) VALUES
(
    gen_random_uuid(),
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'Administrador Sistema',
    'admin@proarte.com.co',
    'a0000000-0000-0000-0000-000000000001'
);
