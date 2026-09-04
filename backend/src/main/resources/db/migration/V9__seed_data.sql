-- ============================================================
-- V9: Datos semilla — Lookups, Roles, Permisos, Admin y Datos de Prueba
-- ERP Pro Arte - PostgreSQL 15+
-- ============================================================

-- ============================================================
-- 1. Datos de tablas Lookup (Asignando UUIDs fijos)
-- ============================================================

INSERT INTO tipo_documento (id, nombre) VALUES 
('cb79474e-eab2-40ca-9a91-fd2289878981', 'CC'), 
('f5609519-112f-4c4f-a357-e09532f9ab3c', 'CE'), 
('f48fbdca-e385-47c7-b2a9-819d6014de92', 'NIT'), 
('665cc4a5-04e9-4070-9ab2-d3545a5acbf8', 'PA'), 
('6d912ab5-3e7c-4d91-b845-0827b671fa6e', 'TI'), 
('4cf37979-3cdb-466b-967e-51db9b7b34df', 'RC');

INSERT INTO rol_entidad (id, nombre) VALUES 
('6ace5bc2-34af-4e5f-ab76-920b00aaf675', 'contacto'), 
('c5a95280-36e3-442f-b320-dcac56414918', 'cliente'), 
('41d13d3f-db42-485c-a22a-f334727b755c', 'proveedor'), 
('5d852183-3909-444f-9867-c69ed098f052', 'aliado'), 
('5b76af84-1b04-47b5-98dc-f594de9d40e9', 'artista');

INSERT INTO estado (id, nombre, contexto) VALUES
('c831fa82-3868-477a-8bec-b6b2061baa9e', 'nuevo', 'lead'), 
('3dd35b85-b237-44ce-bc99-a586d26245cb', 'contactado', 'lead'), 
('14b51223-d5dd-410b-8a86-ce3f5164065e', 'cotizado', 'lead'), 
('fd71e7ab-7b09-4f60-ba1f-3cc4c406de48', 'ganado', 'lead'), 
('23078248-1887-46da-9b16-7246405ec196', 'perdido', 'lead'),
('280ca2f4-706e-4ba1-96e5-9875beac41b6', 'borrador', 'cotizacion'), 
('bc84e0b5-e447-4c36-8843-8a6b30ecf68d', 'enviada', 'cotizacion'), 
('78fbb194-cffc-45f1-945e-3bef04fe9dfc', 'aprobada', 'cotizacion'), 
('59652ee7-8a52-46fa-a908-44a0649109d7', 'rechazada', 'cotizacion'), 
('88fb5e7f-a862-4352-8281-9b5a5a88de70', 'vencida', 'cotizacion'),
('3a84bef0-3044-4710-b78e-51d9edc26a28', 'planificacion', 'evento'), 
('0c78d10c-5507-4fb1-8919-fc55f37d5d6f', 'en_curso', 'evento'), 
('3fea5fec-c423-414a-86fe-6f65debd886b', 'finalizado', 'evento'), 
('3e8b30ad-5796-4fe0-9c2b-b667466e5a04', 'cancelado', 'evento'),
('4a6ff8fb-9b81-4164-8e52-9bf2442a11d2', 'pendiente', 'solicitud_servicio'), 
('b8a2e77e-a741-46b9-81e7-d3ff841a61ad', 'aceptada', 'solicitud_servicio'), 
('263b409f-8601-4c08-93c3-46fb3f106864', 'rechazada', 'solicitud_servicio'), 
('b77a31b7-d47c-4db4-898a-7ac12d61e648', 'completada', 'solicitud_servicio'),
('d1c4ad63-fced-4026-ae28-d1f81b2d7fde', 'pendiente', 'orden_compra'), 
('39bdefc4-7b42-41a9-952a-40484cf0245c', 'aprobada', 'orden_compra'), 
('5eae8e14-7ab9-4d9d-bb36-82af7915c4f9', 'enviada', 'orden_compra'), 
('b0be3baa-2759-419e-aa02-9fa619542a73', 'recibida', 'orden_compra'), 
('769fa829-04ec-4bce-9c08-c4ede4de63e0', 'cancelada', 'orden_compra');

INSERT INTO categoria_servicio (id, nombre) VALUES 
('8a916266-756b-4c39-a993-2b6c8e0ba2f6', 'Propio'), 
('4656c5f2-1196-436b-ad8c-c40347f05fcd', 'Tercero');

INSERT INTO unidad_medida (id, nombre, abreviatura) VALUES
('31a725d1-a906-4ee5-896f-c9520a2b6c31', 'Unidad', 'ud'), 
('17bb2ade-18c2-4c3d-b83a-f5785c7f7fb8', 'Kilogramo', 'kg'), 
('afe3c6b8-56be-46f2-b522-d10b38f57d49', 'Litro', 'lt'), 
('c465e299-cd1f-4622-ae96-8c0aad2c6b31', 'Metro', 'm'), 
('5e9ca00c-1abe-49be-9af8-4e9307c85bc9', 'Caja', 'cj');

INSERT INTO rol_evento (id, nombre) VALUES 
('664fcec5-d36b-4f0c-b46e-1c6a318f5959', 'organizador'), 
('d79bd000-b711-44e9-b39f-e4915483c9e6', 'responsable'), 
('4ec2fac5-5cd3-4a32-af76-b0b9c717c874', 'asistente'), 
('df087e03-9890-4b5a-891e-5c88e5e0bdbd', 'promotor'), 
('0c8cc5a7-d500-47ce-b1d7-8409df18625b', 'coordinador'), 
('6609c118-2e10-4335-9492-80767417ced3', 'personal');

-- ============================================================
-- 2. Roles del sistema
-- ============================================================
INSERT INTO rol (id, nombre, descripcion) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'Administrador', 'Acceso total al sistema'),
    ('a0000000-0000-0000-0000-000000000002', 'Comercial', 'Gestión comercial'),
    ('a0000000-0000-0000-0000-000000000003', 'Operativo', 'Gestión operativa de eventos'),
    ('a0000000-0000-0000-0000-000000000004', 'Coordinador', 'Consulta de eventos asignados');

-- ============================================================
-- 3. Permisos de roles
-- ============================================================
INSERT INTO permiso (id, rol_id, configuracion, activo) VALUES
('5adc9029-365a-455c-bc18-41bce6016e81', 'a0000000-0000-0000-0000-000000000001', '{
    "usuarios": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "roles": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "personas": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "empresas": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "leads": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "servicios": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "proveedores": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "cotizaciones": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "eventos": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "evento_personal": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "ordenes": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "solicitudes": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "portafolio": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "inventario": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "alimentacion": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "presentaciones": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "mensajes": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "observaciones": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "reportes": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "catalogos": {"crear": true, "leer": true, "editar": true, "eliminar": true},
    "descuentos_recargos": {"crear": true, "leer": true, "editar": true, "eliminar": true}
}'::jsonb, true);

-- ============================================================
-- 4. Usuario administrador por defecto
-- ============================================================
INSERT INTO usuario (id, username, password_hash, nombre_completo, email, rol_id) VALUES
(
    '3588db99-bb49-4dc3-a196-d1303df7e6c0',
    'admin',
    '$2a$12$TzXzAKe.3yamWbIpGTPygO5gKRWbbYv5k7wLpeU60uurDO7xMbiTK',
    'Administrador Sistema',
    'admin@proarte.com.co',
    'a0000000-0000-0000-0000-000000000001'
);

-- =======================================================================
-- 5. CREACIÓN DE DEPENDENCIAS (Persona y Empresa de prueba)
-- =======================================================================

INSERT INTO public.persona (
    id, nombres, apellidos, tipo_documento_id, documento, 
    telefono, email, direccion, rol_entidad_id, activo, created_by
) VALUES (
    'd290f1ee-6c54-4b01-90e6-d701748f0851', 
    'Carlos', 
    'Mendoza', 
    'cb79474e-eab2-40ca-9a91-fd2289878981', 
    '1020304050', 
    '3001234567', 
    'carlos.mendoza@email.com', 
    'Calle Falsa 123, Bogotá', 
    'c5a95280-36e3-442f-b320-dcac56414918', 
    true, 
    '3588db99-bb49-4dc3-a196-d1303df7e6c0'
);

INSERT INTO public.empresa (
    id, razon_social, nit, direccion, telefono, 
    email, rol_entidad_id, activo, created_by
) VALUES (
    'e390f1ee-6c54-4b01-90e6-d701748f0852', 
    'Tech Solutions S.A.S.', 
    '900123456-7', 
    'Carrera 15 # 85-50, Edificio Centro Empresarial', 
    '6019876543', 
    'contacto@techsolutions.com.co', 
    'c5a95280-36e3-442f-b320-dcac56414918', 
    true, 
    '3588db99-bb49-4dc3-a196-d1303df7e6c0'
);

-- =======================================================================
-- 6. CREACIÓN DEL LEAD
-- =======================================================================

INSERT INTO public.lead (
    id, descripcion, estado_id, persona_id, empresa_id, activo, created_by
) VALUES (
    'f490f1ee-6c54-4b01-90e6-d701748f0853', 
    'Cliente interesado en organizar evento corporativo de fin de año para 150 personas.', 
    'c831fa82-3868-477a-8bec-b6b2061baa9e', 
    'd290f1ee-6c54-4b01-90e6-d701748f0851', 
    'e390f1ee-6c54-4b01-90e6-d701748f0852', 
    true, 
    '3588db99-bb49-4dc3-a196-d1303df7e6c0'
);

-- =======================================================================
-- 7. CREACIÓN DE COTIZACIONES
-- =======================================================================

-- Cotización 1 (Con evento asociado)
INSERT INTO public.cotizacion (
    id, codigo, estado_id, fecha_vencimiento, total, 
    persona_id, empresa_id, activo, created_by
) VALUES (
    'a590f1ee-6c54-4b01-90e6-d701748f0854', 
    'COT-2026-001', 
    '78fbb194-cffc-45f1-945e-3bef04fe9dfc', 
    '2026-09-30', 
    15500000.00, 
    'd290f1ee-6c54-4b01-90e6-d701748f0851', 
    'e390f1ee-6c54-4b01-90e6-d701748f0852', 
    true, 
    '3588db99-bb49-4dc3-a196-d1303df7e6c0'
);

-- Cotización 2 (SIN evento asociado, cumpliendo el requerimiento de cotización sin evento)
INSERT INTO public.cotizacion (
    id, codigo, estado_id, fecha_vencimiento, total, 
    persona_id, empresa_id, activo, created_by
) VALUES (
    'c790f1ee-6c54-4b01-90e6-d701748f0899', 
    'COT-2026-002', 
    '280ca2f4-706e-4ba1-96e5-9875beac41b6', 
    '2026-10-15', 
    8200000.00, 
    'd290f1ee-6c54-4b01-90e6-d701748f0851', 
    'e390f1ee-6c54-4b01-90e6-d701748f0852', 
    true, 
    '3588db99-bb49-4dc3-a196-d1303df7e6c0'
);

-- =======================================================================
-- 8. CREACIÓN DEL EVENTO (Únicamente para la primera cotización)
-- =======================================================================

INSERT INTO public.evento (
    id, cotizacion_id, nombre, fecha_inicio, fecha_fin, 
    lugar, estado_id, activo, created_by
) VALUES (
    'b690f1ee-6c54-4b01-90e6-d701748f0855', 
    'a590f1ee-6c54-4b01-90e6-d701748f0854', 
    'Fiesta de Fin de Año - Tech Solutions', 
    '2026-12-12 18:00:00-05', 
    '2026-12-13 02:00:00-05', 
    'Centro de Convenciones Ágora Bogotá', 
    '3a84bef0-3044-4710-b78e-51d9edc26a28', 
    true, 
    '3588db99-bb49-4dc3-a196-d1303df7e6c0'
);