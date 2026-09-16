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
('41d13d3f-db42-485c-a22a-f334727b755c', 'proveedor');

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
('8a916266-756b-4c39-a993-2b6c8e0ba2f6', 'Entretenimiento'), 
('4656c5f2-1196-436b-ad8c-c40347f05fcd', 'Recreacion'),
('7756c5f2-1196-436b-ad8c-c40347f05fce', 'Logística'),
('8856c5f2-1196-436b-ad8c-c40347f05fcf', 'Salud y Emergencia'),
('9956c5f2-1196-436b-ad8c-c40347f05fd0', 'Alimentación');

-- NUEVOS 20 SERVICIOS PARA PORTAFOLIO Y COTIZACIONES
INSERT INTO servicio (id, nombre, descripcion, categoria_id, es_propio, requiere_oc) VALUES 
('50000000-0000-0000-0000-000000000001', 'Recreacionista Infantil', 'Animación para niños', '4656c5f2-1196-436b-ad8c-c40347f05fcd', false, false),
('50000000-0000-0000-0000-000000000002', 'Dinamizador de Grupo', 'Actividades de team building', '4656c5f2-1196-436b-ad8c-c40347f05fcd', false, false),
('50000000-0000-0000-0000-000000000003', 'Guía Turístico', 'Recorridos guiados', '4656c5f2-1196-436b-ad8c-c40347f05fcd', false, false),
('50000000-0000-0000-0000-000000000004', 'Tallerista Arte', 'Tallerista pintura y arte', '4656c5f2-1196-436b-ad8c-c40347f05fcd', false, false),
('50000000-0000-0000-0000-000000000005', 'DJ Profesional', 'DJ con equipos básicos', '8a916266-756b-4c39-a993-2b6c8e0ba2f6', false, true),
('50000000-0000-0000-0000-000000000006', 'Banda Musical en Vivo', 'Agrupación 4 integrantes', '8a916266-756b-4c39-a993-2b6c8e0ba2f6', false, true),
('50000000-0000-0000-0000-000000000007', 'Show de Magia', 'Mago profesional 1 hora', '8a916266-756b-4c39-a993-2b6c8e0ba2f6', false, false),
('50000000-0000-0000-0000-000000000008', 'Presentador / Maestro de Ceremonia', 'Presentación protocolo', '8a916266-756b-4c39-a993-2b6c8e0ba2f6', false, false),
('50000000-0000-0000-0000-000000000009', 'Coordinador Logístico', 'Líder de evento', '7756c5f2-1196-436b-ad8c-c40347f05fce', false, false),
('50000000-0000-0000-0000-000000000010', 'Auxiliar de Logística', 'Personal de apoyo logístico', '7756c5f2-1196-436b-ad8c-c40347f05fce', false, false),
('50000000-0000-0000-0000-000000000011', 'Personal de Aseo', 'Apoyo limpieza', '7756c5f2-1196-436b-ad8c-c40347f05fce', false, false),
('50000000-0000-0000-0000-000000000012', 'Vigilancia / Seguridad', 'Control de accesos', '7756c5f2-1196-436b-ad8c-c40347f05fce', false, true),
('50000000-0000-0000-0000-000000000013', 'Transporte Van 15 Pasajeros', 'Transporte personal', '7756c5f2-1196-436b-ad8c-c40347f05fce', false, true),
('50000000-0000-0000-0000-000000000014', 'Paramédico / APH', 'Atención pre-hospitalaria', '8856c5f2-1196-436b-ad8c-c40347f05fcf', false, false),
('50000000-0000-0000-0000-000000000015', 'Ambulancia Básica (TAB)', 'Disponibilidad ambulancia', '8856c5f2-1196-436b-ad8c-c40347f05fcf', false, true),
('50000000-0000-0000-0000-000000000016', 'Enfermero/a', 'Puesto de primeros auxilios', '8856c5f2-1196-436b-ad8c-c40347f05fcf', false, false),
('50000000-0000-0000-0000-000000000017', 'Catering Almuerzo Ejecutivo', 'Almuerzo completo', '9956c5f2-1196-436b-ad8c-c40347f05fd0', false, true),
('50000000-0000-0000-0000-000000000018', 'Refrigerio AM (Café y Panadería)', 'Refrigerio ligero', '9956c5f2-1196-436b-ad8c-c40347f05fd0', false, true),
('50000000-0000-0000-0000-000000000019', 'Refrigerio PM (Snack dulce)', 'Refrigerio tarde', '9956c5f2-1196-436b-ad8c-c40347f05fd0', false, true),
('50000000-0000-0000-0000-000000000020', 'Estación de Café (Ilimitado)', 'Punto de hidratación', '9956c5f2-1196-436b-ad8c-c40347f05fd0', false, true);

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
    "usuarios": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "roles": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "personas": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "empresas": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "leads": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "servicios": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "proveedores": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "proveedores-personas": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "cotizaciones": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "eventos": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "evento_personal": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "ordenes": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "solicitudes": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "portafolio": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "inventario": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "alimentacion": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "presentaciones": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "mensajes": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "observaciones": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "reportes": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "catalogos": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true},
    "descuentos_recargos": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": true}
}'::jsonb, true),
('5adc9029-365a-455c-bc18-41bce6016e82', 'a0000000-0000-0000-0000-000000000002', '{
    "usuarios": {"ver_detalle": true, "crear": false, "leer": true, "editar": false, "eliminar": false},
    "roles": {"ver_detalle": true, "crear": false, "leer": false, "editar": false, "eliminar": false},
    "personas": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": false},
    "empresas": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": false},
    "leads": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": false},
    "servicios": {"ver_detalle": true, "crear": false, "leer": true, "editar": false, "eliminar": false},
    "proveedores": {"ver_detalle": true, "crear": false, "leer": true, "editar": true, "eliminar": false},
    "proveedores-personas": {"ver_detalle": true, "crear": false, "leer": true, "editar": true, "eliminar": false},
    "cotizaciones": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": false},
    "eventos": {"ver_detalle": true, "crear": false, "leer": true, "editar": true, "eliminar": false},
    "evento_personal": {"ver_detalle": true, "crear": false, "leer": true, "editar": true, "eliminar": false},
    "ordenes": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": false},
    "solicitudes": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": false},
    "portafolio": {"ver_detalle": true, "crear": false, "leer": true, "editar": false, "eliminar": false},
    "inventario": {"ver_detalle": true, "crear": false, "leer": false, "editar": false, "eliminar": false},
    "alimentacion": {"ver_detalle": true, "crear": false, "leer": false, "editar": false, "eliminar": false},
    "presentaciones": {"ver_detalle": true, "crear": false, "leer": false, "editar": false, "eliminar": false},
    "mensajes": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": false},
    "observaciones": {"ver_detalle": true, "crear": true, "leer": true, "editar": true, "eliminar": false},
    "reportes": {"ver_detalle": true, "crear": false, "leer": false, "editar": false, "eliminar": false},
    "catalogos": {"ver_detalle": true, "crear": false, "leer": true, "editar": false, "eliminar": false},
    "descuentos_recargos": {"ver_detalle": true, "crear": false, "leer": true, "editar": false, "eliminar": false}
}'::jsonb, true);

-- ============================================================
-- 4. Usuario administrador por defecto y comercial
-- ============================================================
INSERT INTO usuario (id, username, password_hash, nombre_completo, email, rol_id) VALUES
(
    '3588db99-bb49-4dc3-a196-d1303df7e6c0',
    'admin',
    '$2a$12$TzXzAKe.3yamWbIpGTPygO5gKRWbbYv5k7wLpeU60uurDO7xMbiTK',
    'Administrador Sistema',
    'admin@proarte.com.co',
    'a0000000-0000-0000-0000-000000000001'
),
(
    '3588db99-bb49-4dc3-a196-d1303df7e6c1',
    'Comercial Prueba',
    '$2a$12$TzXzAKe.3yamWbIpGTPygO5gKRWbbYv5k7wLpeU60uurDO7xMbiTK',
    'Sebastian Cardona',
    'comercial@proarte.com.co',
    'a0000000-0000-0000-0000-000000000002'
);

-- =======================================================================
-- 5. CREACIÓN DE EMPRESAS Y CONTACTOS (Clientes)
-- =======================================================================

-- Empresa y Contacto Original
INSERT INTO public.empresa (id, razon_social, nit, direccion, telefono, email, rol_entidad_id, created_by) VALUES 
('e390f1ee-6c54-4b01-90e6-d701748f0852', 'Tech Solutions S.A.S.', '900123456-7', 'Carrera 15 # 85-50', '6019876543', 'contacto@techsolutions.com.co', 'c5a95280-36e3-442f-b320-dcac56414918', '3588db99-bb49-4dc3-a196-d1303df7e6c0');

INSERT INTO public.persona (id, nombres, apellidos, tipo_documento_id, documento, telefono, email, direccion, rol_entidad_id, created_by) VALUES 
('d290f1ee-6c54-4b01-90e6-d701748f0851', 'Carlos', 'Mendoza', 'cb79474e-eab2-40ca-9a91-fd2289878981', '1020304050', '3001234567', 'carlos.mendoza@email.com', 'Calle Falsa 123', 'c5a95280-36e3-442f-b320-dcac56414918', '3588db99-bb49-4dc3-a196-d1303df7e6c0');

-- Nuevas Empresas
INSERT INTO public.empresa (id, razon_social, nit, direccion, telefono, email, rol_entidad_id, created_by) VALUES 
('e0000000-0000-0000-0000-000000000001', 'Banco Davivienda S.A.', '860034313-7', 'Av. El Dorado # 68C-61, Bogotá', '6013303333', 'proveedores@davivienda.com', 'c5a95280-36e3-442f-b320-dcac56414918', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('e0000000-0000-0000-0000-000000000002', 'IDEGER Bogotá', '899999061-9', 'Diagonal 47 # 77A-09, Bogotá', '6014292800', 'contratacion@idiger.gov.co', 'c5a95280-36e3-442f-b320-dcac56414918', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('e0000000-0000-0000-0000-000000000003', 'Rama Judicial - Consejo Superior de la Judicatura', '800140578-1', 'Calle 12 # 7-65, Bogotá', '6012833333', 'info@ramajudicial.gov.co', 'c5a95280-36e3-442f-b320-dcac56414918', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('e0000000-0000-0000-0000-000000000004', 'DIAN - Dirección de Impuestos y Aduanas', '800197268-4', 'Carrera 8 # 6C-38, Bogotá', '6013078064', 'eventos@dian.gov.co', 'c5a95280-36e3-442f-b320-dcac56414918', '3588db99-bb49-4dc3-a196-d1303df7e6c0');

-- Nuevos Contactos
INSERT INTO public.persona (id, nombres, apellidos, tipo_documento_id, documento, telefono, email, rol_entidad_id, created_by) VALUES 
('f0000000-0000-0000-0000-000000000001', 'Juan', 'Pérez', 'cb79474e-eab2-40ca-9a91-fd2289878981', '1010101010', '3100000001', 'jperez@davivienda.com', '6ace5bc2-34af-4e5f-ab76-920b00aaf675', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('f0000000-0000-0000-0000-000000000002', 'Maria', 'López', 'cb79474e-eab2-40ca-9a91-fd2289878981', '2020202020', '3100000002', 'mlopez@idiger.gov.co', '6ace5bc2-34af-4e5f-ab76-920b00aaf675', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('f0000000-0000-0000-0000-000000000003', 'Carlos', 'Ruiz', 'cb79474e-eab2-40ca-9a91-fd2289878981', '3030303030', '3100000003', 'cruiz@ramajudicial.gov.co', '6ace5bc2-34af-4e5f-ab76-920b00aaf675', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('f0000000-0000-0000-0000-000000000004', 'Ana', 'Gómez', 'cb79474e-eab2-40ca-9a91-fd2289878981', '4040404040', '3100000004', 'agomez@dian.gov.co', '6ace5bc2-34af-4e5f-ab76-920b00aaf675', '3588db99-bb49-4dc3-a196-d1303df7e6c0');

-- Relacionar Personas con Empresas
INSERT INTO persona_empresa (id, persona_id, empresa_id, cargo) VALUES
(gen_random_uuid(), 'f0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'Director RRHH'),
(gen_random_uuid(), 'f0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000002', 'Coordinador Bienestar'),
(gen_random_uuid(), 'f0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000003', 'Jefe de Contratación'),
(gen_random_uuid(), 'f0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000004', 'Asesor Eventos');


-- =======================================================================
-- 6. CREACIÓN DE LEADS
-- =======================================================================

-- Lead Original
INSERT INTO public.lead (id, descripcion, estado_id, persona_id, empresa_id, created_by) VALUES 
('f490f1ee-6c54-4b01-90e6-d701748f0853', 'Cliente interesado en organizar evento corporativo de fin de año para 150 personas.', 'c831fa82-3868-477a-8bec-b6b2061baa9e', 'd290f1ee-6c54-4b01-90e6-d701748f0851', 'e390f1ee-6c54-4b01-90e6-d701748f0852', '3588db99-bb49-4dc3-a196-d1303df7e6c0');

-- 10 Leads Nuevos
INSERT INTO public.lead (id, descripcion, estado_id, persona_id, empresa_id, created_by) VALUES 
('d0000000-0000-0000-0000-000000000001', 'Evento corporativo integración Davivienda', 'fd71e7ab-7b09-4f60-ba1f-3cc4c406de48', 'f0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('d0000000-0000-0000-0000-000000000002', 'Simulacro distrital IDIGER - Recreación y Logística', 'fd71e7ab-7b09-4f60-ba1f-3cc4c406de48', 'f0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000002', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('d0000000-0000-0000-0000-000000000003', 'Convención Anual Jueces - Rama Judicial', 'fd71e7ab-7b09-4f60-ba1f-3cc4c406de48', 'f0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000003', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('d0000000-0000-0000-0000-000000000004', 'Día de la Familia DIAN 2026', 'c831fa82-3868-477a-8bec-b6b2061baa9e', 'f0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000004', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('d0000000-0000-0000-0000-000000000005', 'Campaña comercial Puntos Davivienda', '14b51223-d5dd-410b-8a86-ce3f5164065e', 'f0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('d0000000-0000-0000-0000-000000000006', 'Jornada Prevención Riesgos IDEGER', '23078248-1887-46da-9b16-7246405ec196', 'f0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000002', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('d0000000-0000-0000-0000-000000000007', 'Celebración fin de año Rama Judicial', '23078248-1887-46da-9b16-7246405ec196', 'f0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000003', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('d0000000-0000-0000-0000-000000000008', 'Lanzamiento nueva plataforma DIAN', 'fd71e7ab-7b09-4f60-ba1f-3cc4c406de48', 'f0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000004', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('d0000000-0000-0000-0000-000000000009', 'Taller de liderazgo directores Davivienda', '14b51223-d5dd-410b-8a86-ce3f5164065e', 'f0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('d0000000-0000-0000-0000-000000000010', 'Capacitación Rescate IDEGER', '23078248-1887-46da-9b16-7246405ec196', 'f0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000002', '3588db99-bb49-4dc3-a196-d1303df7e6c0');


-- =======================================================================
-- 7. CREACIÓN DE COTIZACIONES (Originales y Nuevas)
-- =======================================================================

-- 2 Cotizaciones Originales
INSERT INTO public.cotizacion (id, codigo, estado_id, fecha_vencimiento, total, persona_id, empresa_id, created_by) VALUES 
('a590f1ee-6c54-4b01-90e6-d701748f0854', 'COT-2026-001', '78fbb194-cffc-45f1-945e-3bef04fe9dfc', '2026-09-30', 15500000.00, 'd290f1ee-6c54-4b01-90e6-d701748f0851', 'e390f1ee-6c54-4b01-90e6-d701748f0852', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('c790f1ee-6c54-4b01-90e6-d701748f0899', 'COT-2026-002', '280ca2f4-706e-4ba1-96e5-9875beac41b6', '2026-10-15', 8200000.00, 'd290f1ee-6c54-4b01-90e6-d701748f0851', 'e390f1ee-6c54-4b01-90e6-d701748f0852', '3588db99-bb49-4dc3-a196-d1303df7e6c0');

-- 10 Cotizaciones Nuevas (Con variedad de estados y montos temporales, el trigger las actualizará)
INSERT INTO public.cotizacion (id, codigo, estado_id, fecha_vencimiento, total, persona_id, empresa_id, created_by) VALUES 
('c0000000-0000-0000-0000-000000000001', 'COT-2026-003', '78fbb194-cffc-45f1-945e-3bef04fe9dfc', '2026-10-01', 2500000, 'f0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', '3588db99-bb49-4dc3-a196-d1303df7e6c0'), -- Davivienda (Aprobada)
('c0000000-0000-0000-0000-000000000002', 'COT-2026-004', '78fbb194-cffc-45f1-945e-3bef04fe9dfc', '2026-10-05', 4800000, 'f0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000002', '3588db99-bb49-4dc3-a196-d1303df7e6c0'), -- Ideger (Aprobada)
('c0000000-0000-0000-0000-000000000003', 'COT-2026-005', '78fbb194-cffc-45f1-945e-3bef04fe9dfc', '2026-10-10', 9500000, 'f0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000003', '3588db99-bb49-4dc3-a196-d1303df7e6c0'), -- Rama Judicial (Aprobada)
('c0000000-0000-0000-0000-000000000004', 'COT-2026-006', '280ca2f4-706e-4ba1-96e5-9875beac41b6', '2026-11-20', 3200000, 'f0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000004', '3588db99-bb49-4dc3-a196-d1303df7e6c0'), -- DIAN (Borrador)
('c0000000-0000-0000-0000-000000000005', 'COT-2026-007', 'bc84e0b5-e447-4c36-8843-8a6b30ecf68d', '2026-10-15', 1800000, 'f0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', '3588db99-bb49-4dc3-a196-d1303df7e6c0'), -- Davivienda (Enviada)
('c0000000-0000-0000-0000-000000000006', 'COT-2026-008', '59652ee7-8a52-46fa-a908-44a0649109d7', '2026-08-15', 7200000, 'f0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000002', '3588db99-bb49-4dc3-a196-d1303df7e6c0'), -- Ideger (Rechazada)
('c0000000-0000-0000-0000-000000000007', 'COT-2026-009', '88fb5e7f-a862-4352-8281-9b5a5a88de70', '2026-05-10', 5000000, 'f0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000003', '3588db99-bb49-4dc3-a196-d1303df7e6c0'), -- Rama Judicial (Vencida)
('c0000000-0000-0000-0000-000000000008', 'COT-2026-010', '78fbb194-cffc-45f1-945e-3bef04fe9dfc', '2026-10-30', 12500000, 'f0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000004', '3588db99-bb49-4dc3-a196-d1303df7e6c0'), -- DIAN (Aprobada)
('c0000000-0000-0000-0000-000000000009', 'COT-2026-011', 'bc84e0b5-e447-4c36-8843-8a6b30ecf68d', '2026-11-05', 4000000, 'f0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', '3588db99-bb49-4dc3-a196-d1303df7e6c0'), -- Davivienda (Enviada)
('c0000000-0000-0000-0000-000000000010', 'COT-2026-012', '59652ee7-8a52-46fa-a908-44a0649109d7', '2026-07-20', 8800000, 'f0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000002', '3588db99-bb49-4dc3-a196-d1303df7e6c0'); -- Ideger (Rechazada)


-- =======================================================================
-- 7.5. ÍTEMS PARA LAS COTIZACIONES (Obligatorio)
-- (Disparará el trigger trg_fn_calcular_subtotal_item automáticamente)
-- =======================================================================

-- Items Cotizacion 1 Original (Tech Solutions)
INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES
(gen_random_uuid(), 'a590f1ee-6c54-4b01-90e6-d701748f0854', '50000000-0000-0000-0000-000000000017', 150, 45000.00), -- Almuerzo
(gen_random_uuid(), 'a590f1ee-6c54-4b01-90e6-d701748f0854', '50000000-0000-0000-0000-000000000006', 1, 3500000.00), -- Banda Musical
(gen_random_uuid(), 'a590f1ee-6c54-4b01-90e6-d701748f0854', '50000000-0000-0000-0000-000000000009', 2, 250000.00); -- Coord. Logístico

-- Items Cotizacion 2 Original
INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES
(gen_random_uuid(), 'c790f1ee-6c54-4b01-90e6-d701748f0899', '50000000-0000-0000-0000-000000000001', 5, 120000.00),
(gen_random_uuid(), 'c790f1ee-6c54-4b01-90e6-d701748f0899', '50000000-0000-0000-0000-000000000018', 100, 15000.00);

-- Items Cotizacion 3 (Davivienda)
INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000002', 3, 180000.00),
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000019', 200, 12000.00);

-- Items Cotizacion 4 (Ideger)
INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000010', 10, 100000.00),
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000014', 2, 250000.00),
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000015', 1, 950000.00);

-- Items Cotizacion 5 (Rama Judicial)
INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000008', 1, 1500000.00),
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000017', 100, 65000.00);

-- Items Cotizacion 6 (DIAN - Borrador)
INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000005', 1, 800000.00),
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000010', 5, 120000.00);

-- Items Cotizacion 7 (Davivienda - Enviada)
INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000004', 3, 350000.00),
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000020', 1, 450000.00);

-- Items Cotizacion 8 (Ideger - Rechazada)
INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000013', 4, 600000.00),
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000018', 50, 18000.00);

-- Items Cotizacion 9 (Rama Judicial - Vencida)
INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000007', 2, 850000.00),
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000007', '50000000-0000-0000-0000-000000000020', 1, 600000.00);

-- Items Cotizacion 10 (DIAN - Aprobada)
INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000008', '50000000-0000-0000-0000-000000000006', 1, 4500000.00),
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000008', '50000000-0000-0000-0000-000000000017', 200, 35000.00);

-- Items Cotizacion 11 (Davivienda - Enviada)
INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000009', '50000000-0000-0000-0000-000000000009', 2, 300000.00),
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000009', '50000000-0000-0000-0000-000000000012', 4, 180000.00);

-- Items Cotizacion 12 (Ideger - Rechazada)
INSERT INTO cotizacion_item (id, cotizacion_id, servicio_id, cantidad, precio_unitario) VALUES
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000010', '50000000-0000-0000-0000-000000000014', 6, 220000.00),
(gen_random_uuid(), 'c0000000-0000-0000-0000-000000000010', '50000000-0000-0000-0000-000000000015', 2, 850000.00);


-- =======================================================================
-- 8. CREACIÓN DE EVENTOS (Solo para Cotizaciones Aprobadas)
-- =======================================================================

-- Evento Original
INSERT INTO public.evento (id, cotizacion_id, nombre, fecha_inicio, fecha_fin, lugar, estado_id, created_by) VALUES 
('b690f1ee-6c54-4b01-90e6-d701748f0855', 'a590f1ee-6c54-4b01-90e6-d701748f0854', 'Fiesta de Fin de Año - Tech Solutions', '2026-12-12 18:00:00-05', '2026-12-13 02:00:00-05', 'Centro de Convenciones Ágora', '3a84bef0-3044-4710-b78e-51d9edc26a28', '3588db99-bb49-4dc3-a196-d1303df7e6c0');

-- 3 Nuevos Eventos (2026)
INSERT INTO public.evento (id, cotizacion_id, nombre, fecha_inicio, fecha_fin, lugar, estado_id, created_by) VALUES 
('e0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'Integración Davivienda 2026', '2026-10-15 08:00:00-05', '2026-10-15 18:00:00-05', 'Club Campestre Cajicá', '3a84bef0-3044-4710-b78e-51d9edc26a28', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('e0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 'Simulacro Distrital IDIGER', '2026-10-25 07:00:00-05', '2026-10-25 14:00:00-05', 'Parque Simón Bolívar', '3a84bef0-3044-4710-b78e-51d9edc26a28', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('e0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000003', 'Convención Anual de Jueces', '2026-11-20 09:00:00-05', '2026-11-22 17:00:00-05', 'Hotel Tequendama', '3a84bef0-3044-4710-b78e-51d9edc26a28', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('e0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000008', 'Lanzamiento Plataforma DIAN', '2026-11-30 18:00:00-05', '2026-11-30 23:00:00-05', 'Corferias Pabellón 4', '3a84bef0-3044-4710-b78e-51d9edc26a28', '3588db99-bb49-4dc3-a196-d1303df7e6c0');


-- =======================================================================
-- 9. PROVEEDORES (Independientes y Empresas)
-- =======================================================================

-- 10 Personas Independientes (Rol Proveedor)
INSERT INTO public.persona (id, nombres, apellidos, tipo_documento_id, documento, telefono, rol_entidad_id) VALUES 
('b0000000-0000-0000-0000-000000000001', 'Pedro', 'García', 'cb79474e-eab2-40ca-9a91-fd2289878981', '111111111', '3011111111', '41d13d3f-db42-485c-a22a-f334727b755c'),
('b0000000-0000-0000-0000-000000000002', 'Sofia', 'Martínez', 'cb79474e-eab2-40ca-9a91-fd2289878981', '222222222', '3022222222', '41d13d3f-db42-485c-a22a-f334727b755c'),
('b0000000-0000-0000-0000-000000000003', 'Luis', 'Fernández', 'cb79474e-eab2-40ca-9a91-fd2289878981', '333333333', '3033333333', '41d13d3f-db42-485c-a22a-f334727b755c'),
('b0000000-0000-0000-0000-000000000004', 'Camila', 'Gómez', 'cb79474e-eab2-40ca-9a91-fd2289878981', '444444444', '3044444444', '41d13d3f-db42-485c-a22a-f334727b755c'),
('b0000000-0000-0000-0000-000000000005', 'Andrés', 'Silva', 'cb79474e-eab2-40ca-9a91-fd2289878981', '555555555', '3055555555', '41d13d3f-db42-485c-a22a-f334727b755c'),
('b0000000-0000-0000-0000-000000000006', 'Laura', 'Rojas', 'cb79474e-eab2-40ca-9a91-fd2289878981', '666666666', '3066666666', '41d13d3f-db42-485c-a22a-f334727b755c'),
('b0000000-0000-0000-0000-000000000007', 'Diego', 'Ramírez', 'cb79474e-eab2-40ca-9a91-fd2289878981', '777777777', '3077777777', '41d13d3f-db42-485c-a22a-f334727b755c'),
('b0000000-0000-0000-0000-000000000008', 'Valeria', 'Castro', 'cb79474e-eab2-40ca-9a91-fd2289878981', '888888888', '3088888888', '41d13d3f-db42-485c-a22a-f334727b755c'),
('b0000000-0000-0000-0000-000000000009', 'Jorge', 'Morales', 'cb79474e-eab2-40ca-9a91-fd2289878981', '999999999', '3099999999', '41d13d3f-db42-485c-a22a-f334727b755c'),
('b0000000-0000-0000-0000-000000000010', 'Diana', 'Ortiz', 'cb79474e-eab2-40ca-9a91-fd2289878981', '101010101', '3101010101', '41d13d3f-db42-485c-a22a-f334727b755c');

-- 2 Empresas (Rol Proveedor)
INSERT INTO public.empresa (id, razon_social, nit, rol_entidad_id) VALUES 
('b0000000-0000-0000-0000-000000000011', 'Logística Total S.A.S.', '901234567-1', '41d13d3f-db42-485c-a22a-f334727b755c'),
('b0000000-0000-0000-0000-000000000012', 'Mega Eventos y Entretenimiento', '830999888-2', '41d13d3f-db42-485c-a22a-f334727b755c');


-- Registros de Entidad "Proveedor" para Personas y Empresas
INSERT INTO public.proveedor (id, persona_id, especialidad, created_by) VALUES 
('a0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'Paramédico APH', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('a0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002', 'Enfermera Profesional', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('a0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000003', 'Coordinador Logístico', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('a0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000004', 'Auxiliar Logístico', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('a0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000005', 'Guía Turístico', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('a0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000006', 'Dinamizador', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('a0000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000007', 'Personal Aseo', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('a0000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000008', 'Tallerista Arte', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('a0000000-0000-0000-0000-000000000009', 'b0000000-0000-0000-0000-000000000009', 'Auxiliar Logístico', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('a0000000-0000-0000-0000-000000000010', 'b0000000-0000-0000-0000-000000000010', 'Animadora Infantil', '3588db99-bb49-4dc3-a196-d1303df7e6c0');

INSERT INTO public.proveedor (id, empresa_id, especialidad, created_by) VALUES 
('a0000000-0000-0000-0000-000000000011', 'b0000000-0000-0000-0000-000000000011', 'Montajes y Transportes', '3588db99-bb49-4dc3-a196-d1303df7e6c0'),
('a0000000-0000-0000-0000-000000000012', 'b0000000-0000-0000-0000-000000000012', 'Producción Artística', '3588db99-bb49-4dc3-a196-d1303df7e6c0');


-- =======================================================================
-- 10. PORTAFOLIO DE PROVEEDORES
-- =======================================================================
INSERT INTO public.portafolio (proveedor_id, servicio_id, precio_unitario) VALUES
-- Independientes
('a0000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000014', 180000), -- Paramédico
('a0000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000009', 150000), -- Coord Logístico
('a0000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000010', 80000),  -- Aux Logístico
('a0000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000003', 120000), -- Guía
('a0000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000002', 130000), -- Dinamizador
-- Empresas
('a0000000-0000-0000-0000-000000000011', '50000000-0000-0000-0000-000000000013', 450000), -- Transporte Van (Logística Total)
('a0000000-0000-0000-0000-000000000011', '50000000-0000-0000-0000-000000000012', 200000), -- Vigilancia (Logística Total)
('a0000000-0000-0000-0000-000000000012', '50000000-0000-0000-0000-000000000005', 600000), -- DJ (Mega Eventos)
('a0000000-0000-0000-0000-000000000012', '50000000-0000-0000-0000-000000000006', 3000000); -- Banda (Mega Eventos)


-- =======================================================================
-- 11. ASIGNACIÓN DE PERSONAL A EVENTOS (evento_personal)
-- =======================================================================

-- Asignación para Evento Original (Tech Solutions)
INSERT INTO public.evento_personal (id, evento_id, proveedor_id, servicio_id, valor_turno, tiene_arl) VALUES
(gen_random_uuid(), 'b690f1ee-6c54-4b01-90e6-d701748f0855', 'a0000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000009', 150000, true), -- Coordinador
(gen_random_uuid(), 'b690f1ee-6c54-4b01-90e6-d701748f0855', 'a0000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000014', 180000, true); -- Paramédico

-- Asignación para Eventos Nuevos
-- Evento 1 (Davivienda)
INSERT INTO public.evento_personal (id, evento_id, proveedor_id, servicio_id, valor_turno, tiene_arl) VALUES
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000006', '50000000-0000-0000-0000-000000000002', 130000, true), -- Dinamizador
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000010', 80000, true); -- Aux Logistico

-- Evento 2 (IDIGER)
INSERT INTO public.evento_personal (id, evento_id, proveedor_id, servicio_id, valor_turno, tiene_arl) VALUES
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000014', 180000, true), -- Paramédico
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000003', 120000, true); -- Guía Turístico

-- Evento 3 (Rama Judicial)
INSERT INTO public.evento_personal (id, evento_id, proveedor_id, servicio_id, valor_turno, tiene_arl) VALUES
(gen_random_uuid(), 'e0000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000009', 150000, true); -- Coordinador Logístico