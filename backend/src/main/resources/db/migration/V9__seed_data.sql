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
    "usuarios": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "personas": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "empresas": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "leads": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "catalogos": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "servicios": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "descuentos_recargos": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "proveedores": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "cotizaciones": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "eventos": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "evento_personal": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true, "ejecutar": true},
    "ordenes_compra": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "inventario": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "alimentacion": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "presentaciones": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "mensajes": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true}
}'::jsonb);

-- ============================================================
-- Permisos del rol Comercial
-- ============================================================
INSERT INTO permiso (rol_id, configuracion) VALUES
('a0000000-0000-0000-0000-000000000002', '{
    "usuarios": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "personas": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "empresas": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "leads": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "catalogos": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "servicios": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "descuentos_recargos": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": false},
    "proveedores": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "cotizaciones": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "eventos": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "evento_personal": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false, "ejecutar": false},
    "ordenes_compra": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "inventario": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "alimentacion": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "presentaciones": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "mensajes": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true}
}'::jsonb);

-- ============================================================
-- Permisos del rol Operativo
-- ============================================================
INSERT INTO permiso (rol_id, configuracion) VALUES
('a0000000-0000-0000-0000-000000000003', '{
    "usuarios": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "personas": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": false},
    "empresas": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "leads": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "catalogos": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "servicios": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "descuentos_recargos": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "proveedores": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "cotizaciones": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "eventos": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "evento_personal": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true, "ejecutar": true},
    "ordenes_compra": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "inventario": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "alimentacion": {"ver_listado": true, "ver_detalle": true, "crear": true, "editar": true, "eliminar": true},
    "presentaciones": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "mensajes": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false}
}'::jsonb);

-- ============================================================
-- Permisos del rol Coordinador — solo lectura limitada
-- ============================================================
INSERT INTO permiso (rol_id, configuracion) VALUES
('a0000000-0000-0000-0000-000000000004', '{
    "usuarios": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "personas": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "empresas": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "leads": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "catalogos": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "servicios": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "descuentos_recargos": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "proveedores": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "cotizaciones": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "eventos": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false},
    "evento_personal": {"ver_listado": true, "ver_detalle": true, "crear": false, "editar": false, "eliminar": false, "ejecutar": false},
    "ordenes_compra": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "inventario": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "alimentacion": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "presentaciones": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false},
    "mensajes": {"ver_listado": false, "ver_detalle": false, "crear": false, "editar": false, "eliminar": false}
}'::jsonb);

-- ============================================================
-- Usuario administrador por defecto
-- Password: ProArte2024! (hash BCrypt)
-- ============================================================
INSERT INTO usuario (id, username, password_hash, nombre_completo, email, rol_id) VALUES
(
    gen_random_uuid(),
    'admin',
    '$2a$12$TzXzAKe.3yamWbIpGTPygO5gKRWbbYv5k7wLpeU60uurDO7xMbiTK',
    'Administrador Sistema',
    'admin@proarte.com.co',
    'a0000000-0000-0000-0000-000000000001'
);
