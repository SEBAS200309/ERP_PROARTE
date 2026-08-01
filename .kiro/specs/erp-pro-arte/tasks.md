# Implementation Plan

## Overview

Plan de implementacion del ERP Pro Arte organizado en 7 fases secuenciales: setup de proyectos, schema de base de datos (Flyway), capa comun del backend (seguridad, error handling), modulos backend por requerimiento funcional, componentes compartidos del frontend, modulos feature del frontend, y testing de integracion.

Arquitectura: Angular 18+ SPA → Spring Boot 3.3+ REST API → PostgreSQL 15+ (logica de negocio en DB).

## Task Dependency Graph

```json
{
  "waves": [
    ["1.1", "1.2", "1.4"],
    ["1.3", "2.1"],
    ["2.2", "2.3", "5.1"],
    ["2.4", "2.5", "2.6", "2.7", "5.2"],
    ["2.8", "2.9", "3.1", "5.3", "5.4", "5.5", "5.6"],
    ["2.10", "3.2", "3.3", "5.7", "5.8"],
    ["3.4", "3.5", "3.6", "5.9", "5.10"],
    ["4.1", "6.1"],
    ["4.2", "4.3", "4.4", "4.15", "6.2"],
    ["4.5", "4.6", "6.3", "6.4", "6.5"],
    ["4.7", "6.6", "6.7"],
    ["4.8", "6.8"],
    ["4.9", "4.10", "4.11", "4.12", "6.9"],
    ["4.13", "4.14", "6.10", "6.11", "6.12", "6.13"],
    ["6.14", "6.15", "6.16"],
    ["7.1", "7.4"],
    ["7.2", "7.5"],
    ["7.3", "7.6"]
  ]
}
```

## Tasks

### Phase 1: Project Setup and Scaffolding

- [x] 1.1 Create Spring Boot backend project
  - Generate Maven project with Spring Boot 3.3+, Java 21
  - Dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, postgresql, flyway-core, lombok, jjwt, springdoc-openapi
  - Package structure: `com.proarte.erp`
  - Configure application.yml with profiles (dev, test, prod)
  - Configure HikariCP datasource for PostgreSQL
  - Configure Flyway migration path `db/migration`

- [x] 1.2 Create Angular frontend project
  - Generate Angular 18+ project with standalone components, SCSS
  - Configure strict TypeScript mode
  - Create folder structure: core/, shared/, features/
  - Configure `angular.json` with `app-` prefix
  - Add `@angular/animations` dependency
  - Configure proxy for local API (`/api` → `http://localhost:8080`)

- [x] 1.3 Configure frontend theming system
  - Create `_variables.scss` with purple palette ($primary-50 to $primary-900, accents)
  - Create `_themes.scss` with CSS custom properties for dark/light modes
  - Create `ThemeService` that persists preference in localStorage
  - Respect `prefers-color-scheme` as default
  - Add theme toggle mechanism on `<html data-theme>`

- [x] 1.4 Configure Docker Compose for local development
  - PostgreSQL 15+ container with volume persistence
  - Environment variables for DB credentials
  - Port mapping: 5432 (postgres), 8080 (backend), 4200 (frontend)

### Phase 2: Database Schema and Migrations (Flyway)

- [x] 2.1 Create base schema migration (V1__base_schema.sql)
  - Extensions: uuid-ossp or pgcrypto (gen_random_uuid)
  - Table `rol` (id UUID PK, nombre, descripcion, activo, timestamps)
  - Table `permiso` (id UUID PK, rol_id FK, configuracion JSONB, activo, timestamps)
  - Table `usuario` (id UUID PK, username, password_hash, nombre, email, rol_id FK, activo, timestamps)
  - All tables with `activo BOOLEAN DEFAULT TRUE`, `created_at`, `updated_at`

- [x] 2.2 Create personas and empresas migration (V2__personas_empresas.sql)
  - Table `persona` (id UUID PK, nombres, apellidos, documento, tipo_documento_id UUID FK → tipo_documento, email, telefono, rol_entidad_id UUID FK → rol_entidad, created_by FK, activo, timestamps)
  - Table `empresa` (id UUID PK, razon_social, nit, direccion, telefono, email, rol_entidad_id UUID FK → rol_entidad, created_by FK, activo, timestamps)
  - Table `persona_empresa` (id UUID PK, persona_id FK, empresa_id FK, cargo, activo, timestamps)
  - Table `lead` (id UUID PK, titulo, descripcion, estado_id UUID FK → estado(contexto='lead'), persona_id FK, empresa_id FK, created_by FK, activo, timestamps)
  - Nota: Campos tipo_documento, rol_persona, rol_empresa y estado normalizados a UUID FK en migracion V10

- [x] 2.3 Create proveedores and servicios migration (V3__proveedores_servicios.sql)
  - Table `proveedor` (id UUID PK, persona_id FK nullable, empresa_id FK nullable, tipo, activo, timestamps)
  - Table `servicio` (id UUID PK, nombre, descripcion, precio_base, categoria_id UUID FK → categoria_servicio, servicio_padre FK self-ref nullable, requiere_orden_compra BOOLEAN, activo, timestamps)
  - Table `portafolio` (id UUID PK, proveedor_id FK, servicio_id FK, precio, activo, timestamps)
  - Table `porcentaje` (id UUID PK, nombre, valor NUMERIC, tipo ENUM(descuento,recargo), activo, timestamps)
  - Table `solicitud_servicio` (id UUID PK, proveedor_id FK, servicio_id FK, evento_id FK nullable, estado_id UUID FK → estado(contexto='solicitud'), descripcion, activo, timestamps)
  - Nota: Campos categoria y estado normalizados a UUID FK en migracion V10

- [x] 2.4 Create cotizaciones migration (V4__cotizaciones.sql)
  - Table `cotizacion` (id UUID PK, codigo, persona_id FK, empresa_id FK, estado_id UUID FK → estado(contexto='cotizacion'), fecha_emision, fecha_vencimiento, total NUMERIC, observaciones, created_by FK, activo, timestamps)
  - Table `cotizacion_item` (id UUID PK, cotizacion_id FK, servicio_id FK, descripcion, cantidad, precio_unitario, porcentaje_id FK nullable, subtotal NUMERIC, activo, timestamps)
  - Nota: Campo estado normalizado a UUID FK en migracion V10

- [x] 2.5 Create eventos migration (V5__eventos.sql)
  - Table `evento` (id UUID PK, codigo, cotizacion_id FK, nombre, fecha_inicio, fecha_fin, lugar, estado_id UUID FK → estado(contexto='evento'), activo, timestamps)
  - Table `evento_persona` (id UUID PK, evento_id FK, persona_id FK, rol_evento_id UUID FK → rol_evento, activo, timestamps)
  - Table `evento_proveedor` (id UUID PK, evento_id FK, proveedor_id FK, activo, timestamps)
  - Table `evento_observacion` (id UUID PK, evento_id FK, contenido TEXT, created_by FK, activo, timestamps)
  - Table `evento_personal` (id UUID PK, evento_id FK, persona_id FK, proveedor_id FK nullable, servicio_id FK, turno, valor_turno NUMERIC, observaciones, tiene_arl BOOLEAN, tiene_op BOOLEAN, activo, timestamps)
  - Nota: Campos estado y rol_evento normalizados a UUID FK en migracion V10

- [x] 2.6 Create ordenes, mensajes, presentaciones migration (V6__ordenes_mensajes_presentaciones.sql)
  - Table `orden_compra` (id UUID PK, codigo, solicitud_id FK, descripcion, monto NUMERIC, estado_id UUID FK → estado(contexto='orden'), activo, timestamps)
  - Table `mensaje` (id UUID PK, titulo, contenido TEXT, tipo, activo, timestamps)
  - Table `presentacion` (id UUID PK, titulo, descripcion, servicio_id FK nullable, contenido TEXT, activo, timestamps)
  - Nota: Campo estado normalizado a UUID FK en migracion V10

- [x] 2.7 Create inventario and alimentacion migration (V7__inventario_alimentacion.sql)
  - Table `insumo` (id UUID PK, nombre, descripcion, unidad_medida_id UUID FK → unidad_medida, stock_actual NUMERIC DEFAULT 0, activo, timestamps)
  - Table `insumo_movimiento` (id UUID PK, insumo_id FK, tipo ENUM(ingreso,retiro), cantidad NUMERIC, motivo, fecha, created_by FK, activo, timestamps)
  - Table `evento_insumo` (id UUID PK, evento_id FK, insumo_id FK, cantidad_asignada NUMERIC, activo, timestamps)
  - Table `evento_alimentacion` (id UUID PK, evento_id FK, descripcion, tipo ENUM(ingreso,retiro), cantidad NUMERIC, fecha, created_by FK, activo, timestamps)
  - Nota: Campo unidad_medida normalizado a UUID FK en migracion V10

- [x] 2.8 Create business logic functions and triggers (V8__functions_triggers.sql)
  - Function `fn_recalcular_total_cotizacion(cotizacion_uuid)` — recalculates cotizacion total from items
  - Function `fn_crear_evento_desde_cotizacion(cotizacion_uuid)` — creates evento only if estado=APROBADA
  - Function `fn_calcular_valor_turno(evento_personal_uuid)` — calculates shift value
  - Trigger `trg_actualizar_stock` on `insumo_movimiento` — updates `insumo.stock_actual`
  - Trigger `trg_actualizar_alimentacion` on `evento_alimentacion` — updates available quantities
  - Trigger `trg_recalcular_subtotal_item` on `cotizacion_item` INSERT/UPDATE — calculates subtotal
  - Validation: stock check before retiro (RAISE EXCEPTION if insufficient)

- [x] 2.9 Create seed data migration (V9__seed_data.sql)
  - Default roles: Admin, Comercial, Operativo, Coordinador
  - Default permissions JSON for each role
  - Admin user with bcrypt-hashed password
  - Sample servicios catalog entries

- [x] 2.10 Create lookup tables normalization migration (V10__normalizacion_lookup_tables.sql)
  - Table `tipo_documento` (id UUID PK, nombre VARCHAR UNIQUE, activo, timestamps) — CC, NIT, CE, Pasaporte, etc.
  - Table `rol_entidad` (id UUID PK, nombre VARCHAR UNIQUE, activo, timestamps) — contacto, cliente, proveedor
  - Table `estado` (id UUID PK, nombre VARCHAR, contexto VARCHAR, activo, timestamps) — estados por entidad (lead, cotizacion, evento, solicitud, orden)
  - Table `categoria_servicio` (id UUID PK, nombre VARCHAR UNIQUE, activo, timestamps) — propio, de tercero
  - Table `unidad_medida` (id UUID PK, nombre VARCHAR UNIQUE, abreviatura VARCHAR, activo, timestamps) — unidad, kg, litro, metro
  - Table `rol_evento` (id UUID PK, nombre VARCHAR UNIQUE, activo, timestamps) — promotor, contacto, coordinador, personal
  - ALTER persona: tipo_documento → tipo_documento_id UUID FK, rol_persona → rol_entidad_id UUID FK
  - ALTER empresa: rol_empresa → rol_entidad_id UUID FK
  - ALTER lead: estado → estado_id UUID FK
  - ALTER servicio: categoria → categoria_id UUID FK
  - ALTER solicitud_servicio: estado → estado_id UUID FK
  - ALTER cotizacion: estado → estado_id UUID FK
  - ALTER evento: estado → estado_id UUID FK
  - ALTER evento_persona: rol_evento → rol_evento_id UUID FK
  - ALTER orden_compra: estado → estado_id UUID FK
  - ALTER insumo: unidad_medida → unidad_medida_id UUID FK
  - Seed data: valores iniciales para cada catalogo
  - Migracion de datos existentes de VARCHAR a UUID FK

### Phase 3: Backend Common Layer

- [x] 3.1 Implement security configuration (JWT + Spring Security)
  - `SecurityConfig` with stateless session, CORS, CSRF disabled
  - `JwtTokenProvider` — generate, validate, extract claims (8h access, 7d refresh)
  - `JwtAuthenticationFilter` — extract token from Authorization header
  - `AuthController` — /api/v1/auth/login, /api/v1/auth/logout, /api/v1/auth/refresh-token
  - `AuthService` — authenticate with BCrypt password verification
  - `UserDetailsServiceImpl` — load user from DB with permissions
  - `PermissionEvaluator` — validate action against user's JSONB permissions per request

- [x] 3.2 Implement global error handling
  - `GlobalExceptionHandler` with @RestControllerAdvice
  - Custom exceptions: `ResourceNotFoundException`, `BusinessException`, `UnauthorizedException`, `InsufficientStockException`
  - Standard JSON response format: `{ success, data/error, message }`
  - Map PostgreSQL RAISE EXCEPTION codes to HTTP status codes
  - All error messages in Spanish

- [x] 3.3 Implement common response wrapper and base DTOs
  - `ApiResponse<T>` record — wraps all responses with success, data, message
  - `ErrorResponse` record — code, message fields
  - `PageResponse<T>` record — content, totalElements, totalPages, page, size
  - `ProcedureRequest` record — generic params Map<String, Object>
  - Static factory methods for success/error responses

- [x] 3.4 Implement ProcedureExecutor service
  - `ProcedureExecutorService` — executes PostgreSQL functions/procedures via JDBC
  - Accepts function name + JSON params, returns JSON result
  - Generic endpoint pattern: POST /api/v1/{modulo}/execute/{function_name}
  - Input validation and SQL injection prevention
  - Logging of procedure calls

- [x] 3.5 Implement base repository and soft-delete support
  - Custom `SoftDeleteRepository<T>` extending JpaRepository
  - Override `findAll`, `findById` to filter by `activo = true`
  - `@SQLRestriction("activo = true")` on all entities
  - `softDelete(UUID id)` method sets activo = false
  - `BaseEntity` with id (UUID), activo, createdAt, updatedAt, createdBy fields

- [x] 3.6 Implement audit and created_by tracking
  - `AuditingConfig` with `@EnableJpaAuditing`
  - `AuditorAwareImpl` — extracts current user UUID from SecurityContext
  - `@CreatedBy`, `@CreatedDate`, `@LastModifiedDate` annotations on BaseEntity
  - Immutable `createdBy` field — set once, never updated

### Phase 4: Backend Feature Modules

- [x] 4.1 Implement Usuarios module (Requirement 1)
  - Entity: `Usuario`, `Rol`, `Permiso`
  - DTOs: `CreateUsuarioRequest`, `UpdateUsuarioRequest`, `UsuarioResponse`, `RolResponse`, `PermisoConfigRequest`
  - Repository: `UsuarioRepository`, `RolRepository`, `PermisoRepository`
  - Service: `UsuarioService` — CRUD + password hashing with BCrypt
  - Controller: `UsuarioController` — /api/v1/usuarios, /api/v1/usuarios/roles/{id}/permisos (GET, PUT)
  - Permission validation on each endpoint

- [x] 4.2 Implement Leads module (Requirement 2 - Leads)
  - Entity: `Lead` — campo `estadoId` UUID FK referenciando tabla `estado` (contexto='lead')
  - DTOs: `CreateLeadRequest`, `UpdateLeadRequest`, `LeadResponse`, `LeadEstadisticasResponse`
  - Repository: `LeadRepository` con native query JOIN a tabla estado para filtros por nombre de estado
  - Service: `LeadService` — CRUD + estadisticas (count by estado for chart)
  - Controller: `LeadController` — /api/v1/leads + /estadisticas
  - Pagination and search support

- [x] 4.3 Implement Personas module (Requirement 2 - Personas)
  - Entity: `Persona`, `PersonaEmpresa` — campos `tipoDocumentoId` UUID FK → tipo_documento, `rolEntidadId` UUID FK → rol_entidad
  - DTOs: `CreatePersonaRequest` (tipoDocumentoId UUID, rolEntidadId UUID), `UpdatePersonaRequest`, `PersonaResponse`, `AsociarEmpresaRequest`
  - Repository: `PersonaRepository`, `PersonaEmpresaRepository`
  - Service: `PersonaService` — CRUD + asociar-empresa + asignar-rol (recibe UUID de rol_entidad)
  - Controller: `PersonaController` — /api/v1/personas + /asociar-empresa + /asignar-rol
  - Search by nombre, documento, email

- [x] 4.4 Implement Empresas module (Requirement 2 - Empresas)
  - Entity: `Empresa` — campo `rolEntidadId` UUID FK → rol_entidad (reemplaza rol_empresa String)
  - DTOs: `CreateEmpresaRequest` (rolEntidadId UUID), `UpdateEmpresaRequest`, `EmpresaResponse`
  - Repository: `EmpresaRepository`
  - Service: `EmpresaService` — CRUD + asignar-rol (recibe UUID de rol_entidad)
  - Controller: `EmpresaController` — /api/v1/empresas + /asignar-rol
  - Search by razon_social, nit

- [x] 4.5 Implement Proveedores module (Requirement 3)
  - Entity: `Proveedor`, `Portafolio`, `SolicitudServicio` — `solicitud_servicio.estado_id` UUID FK → estado(contexto='solicitud')
  - DTOs: `CreateProveedorRequest`, `ProveedorResponse`, `PortafolioResponse`, `CreateSolicitudRequest`, `SolicitudResponse`
  - Repository: `ProveedorRepository`, `PortafolioRepository`, `SolicitudServicioRepository`
  - Service: `ProveedorService` — CRUD proveedores + portafolio + solicitudes
  - Controller: `ProveedorController` — /api/v1/proveedores + /portafolio + /solicitudes

- [x] 4.6 Implement Servicios module (Requirement 4)
  - Entity: `Servicio` — campo `categoriaId` UUID FK → categoria_servicio (reemplaza categoria ENUM), `Porcentaje`
  - DTOs: `CreateServicioRequest` (categoriaId UUID), `ServicioResponse`, `CreatePorcentajeRequest`, `PorcentajeResponse`
  - Repository: `ServicioRepository` (support self-referencing hierarchy), `PorcentajeRepository`
  - Service: `ServicioService` — CRUD + subservicios hierarchy + categorizar (asigna categoria_id)
  - Service: `PorcentajeService` — CRUD porcentajes + aplicar
  - Controller: `ServicioController` — /api/v1/servicios + /subservicios + /categorizar
  - Controller: `DescuentoRecargoController` — /api/v1/descuentos-recargos + /aplicar

- [x] 4.7 Implement Cotizaciones module (Requirement 5)
  - Entity: `Cotizacion` — campo `estadoId` UUID FK → estado(contexto='cotizacion'), `CotizacionItem`
  - DTOs: `CreateCotizacionRequest`, `CotizacionResponse`, `CotizacionItemRequest`, `CambiarEstadoRequest` (estadoId UUID)
  - Repository: `CotizacionRepository`, `CotizacionItemRepository`
  - Service: `CotizacionService` — CRUD + estados + vencimientos + execute fn_recalcular_total_cotizacion
  - Controller: `CotizacionController` — /api/v1/cotizaciones + /estados + /vencimientos + /pdf
  - PDF generation endpoint (cotizacion formatted document)
  - Filters by estado, cliente, fecha; pagination

- [x] 4.8 Implement Eventos module (Requirement 6)
  - Entity: `Evento` — campo `estadoId` UUID FK → estado(contexto='evento'), `EventoPersona` — campo `rolEventoId` UUID FK → rol_evento, `EventoProveedor`, `EventoObservacion`, `EventoInsumo`
  - DTOs: `CreateEventoRequest`, `EventoResponse`, `EventoPersonaRequest` (rolEventoId UUID), `ObservacionRequest`
  - Repository: `EventoRepository`, `EventoPersonaRepository`, `EventoProveedorRepository`, `EventoObservacionRepository`
  - Service: `EventoService` — CRUD + execute fn_crear_evento_desde_cotizacion + associate providers/personas/observations
  - Controller: `EventoController` — /api/v1/eventos + /proveedores + /servicios + /personas + /observaciones

- [x] 4.9 Implement Personal de Evento module (Requirement 10)
  - Entity: `EventoPersonal`
  - DTOs: `CreateEventoPersonalRequest`, `EventoPersonalResponse`
  - Repository: `EventoPersonalRepository`
  - Service: `EventoPersonalService` — CRUD + execute fn_calcular_valor_turno + ARL/OP status
  - Controller: `EventoPersonalController` — /api/v1/eventos/{id}/personal + /execute/calcular_turno
  - Include ARL/OP validation warnings in response

- [x] 4.10 Implement Ordenes de Compra module (Requirement 7)
  - Entity: `OrdenCompra` — campo `estadoId` UUID FK → estado(contexto='orden')
  - DTOs: `CreateOrdenCompraRequest`, `OrdenCompraResponse`
  - Repository: `OrdenCompraRepository`
  - Service: `OrdenCompraService` — CRUD + Excel generation
  - Controller: `OrdenCompraController` — /api/v1/ordenes-compra + /descargar-excel
  - Excel download using Apache POI or similar library

- [x] 4.11 Implement Mensajes module (Requirement 8)
  - Entity: `Mensaje`
  - DTOs: `CreateMensajeRequest`, `UpdateMensajeRequest`, `MensajeResponse`
  - Repository: `MensajeRepository`
  - Service: `MensajeService` — CRUD message templates
  - Controller: `MensajeController` — /api/v1/mensajes

- [x] 4.12 Implement Presentaciones module (Requirement 9)
  - Entity: `Presentacion`
  - DTOs: `CreatePresentacionRequest`, `UpdatePresentacionRequest`, `PresentacionResponse`
  - Repository: `PresentacionRepository`
  - Service: `PresentacionService` — CRUD + PDF generation
  - Controller: `PresentacionController` — /api/v1/presentaciones + /pdf

- [x] 4.13 Implement Inventario module (Requirement 11)
  - Entity: `Insumo` — campo `unidadMedidaId` UUID FK → unidad_medida, `InsumoMovimiento`
  - DTOs: `InsumoResponse`, `CreateMovimientoRequest`, `MovimientoResponse`
  - Repository: `InsumoRepository`, `InsumoMovimientoRepository`
  - Service: `InventarioService` — consultar stock + registrar ingresos/retiros (trigger updates stock)
  - Controller: `InventarioController` — /api/v1/inventario + /ingresos + /retiros
  - Stock validation handled by PostgreSQL trigger (ERR_STOCK on insufficient)

- [x] 4.14 Implement Alimentacion module (Requirement 12)
  - Entity: `EventoAlimentacion`
  - DTOs: `CreateAlimentacionRequest`, `AlimentacionResponse`
  - Repository: `EventoAlimentacionRepository`
  - Service: `AlimentacionService` — consultar + ingresos/retiros per evento
  - Controller: `AlimentacionController` — /api/v1/eventos/{id}/alimentacion + /ingresos + /retiros
  - Quantity validation handled by PostgreSQL trigger

- [x] 4.15 Implement Catalogos module (Requirement 13)
  - Entities: `TipoDocumento`, `RolEntidad`, `Estado`, `CategoriaServicio`, `UnidadMedida`, `RolEvento`
  - DTOs: `CatalogoResponse` (id UUID, nombre String), `CreateCatalogoRequest` (nombre String), generico para todos los catalogos
  - Repository: uno por entidad extendiendo JpaRepository — `TipoDocumentoRepository`, `RolEntidadRepository`, `EstadoRepository`, `CategoriaServicioRepository`, `UnidadMedidaRepository`, `RolEventoRepository`
  - Service: `CatalogoService` — CRUD generico para todas las tablas de catalogo + validacion de FK en uso antes de eliminar
  - Controller: `CatalogoController` — /api/v1/catalogos/{tipo} (GET list, POST create, PUT update, DELETE with FK validation)
  - Tipos soportados: tipo-documento, rol-entidad, estado, categoria-servicio, unidad-medida, rol-evento
  - Filtro por contexto para estados: GET /api/v1/catalogos/estado?contexto=lead

### Phase 5: Frontend Shared Components and Core Services

- [x] 5.1 Implement core authentication services and guards
  - `AuthService` — login, logout, refresh token, store JWT in localStorage
  - `AuthGuard` (functional) — redirect to login if no session
  - `PermissionGuard` (functional) — check user permissions from loaded JSON
  - `JwtInterceptor` — attach Authorization header to all API requests
  - `ErrorInterceptor` — catch HTTP errors, map to Spanish messages, handle 401 redirect
  - `PermissionService` — load and cache current user's permission JSON

- [x] 5.2 Implement error message service and Spanish message dictionaries
  - `ErrorMessageService` — centralized error message resolution
  - `errorMessages` dictionary for form validations (required, email, minlength, etc.)
  - `httpErrorMessages` dictionary for HTTP status codes
  - `generalMessages` dictionary for CRUD operations (save, delete, load errors)
  - Never expose stack traces or technical errors to user

- [x] 5.3 Implement shared DataTable component
  - `DataTableComponent` — standalone, reusable table with:
    - Pagination (page, pageSize)
    - Column sorting
    - Search/filter input
    - Action buttons per row: Ver (ojo), Editar (lapiz), Eliminar (X)
    - Buttons conditionally rendered based on user permissions
    - Loading skeleton state
    - Empty state message
  - Works in dark/light theme via CSS custom properties
  - Input: columns config, data array, permissions object
  - Output: events for view, edit, delete actions

- [x] 5.4 Implement shared ConfirmDialog component
  - `ConfirmDialogComponent` — standalone modal for delete confirmations
  - Spanish messages: "¿Está seguro que desea eliminar este registro?"
  - Animated entry/exit transitions
  - Buttons: Cancelar (secondary), Eliminar (danger)
  - Returns Observable<boolean>

- [x] 5.5 Implement shared AnimatedButton component and animations
  - `button.animations.ts` — buttonPress, buttonHover, buttonRipple triggers
  - `AnimatedButtonComponent` — standalone wrapper that applies animations
  - Variants: primary, secondary, danger, ghost
  - Respects `prefers-reduced-motion` media query
  - Works in dark/light theme

- [x] 5.6 Implement shared SearchFilter component
  - `SearchFilterComponent` — standalone input with debounce (300ms)
  - Emits search term on change
  - Clear button, placeholder in Spanish
  - Works in dark/light theme

- [x] 5.7 Implement shared DetailView component (context pattern)
  - `DetailViewComponent` — standalone panel/modal for record detail
  - Shows record fields in top section
  - Shows related tables (context) below, based on user permissions
  - Each context section is a mini data-table
  - If user lacks permission for a related table, that section is hidden
  - Close button, dark/light theme support

- [x] 5.8 Implement app routing with lazy loading
  - `app.routes.ts` with lazy-loaded routes per feature module
  - Apply `AuthGuard` to all routes except /auth/login
  - Apply `PermissionGuard` per feature route
  - Dashboard as default route after login
  - Wildcard route → redirect to dashboard

- [x] 5.9 Implement base CRUD service factory
  - `BaseCrudService<T>` abstract class with generic CRUD methods
  - Methods: getAll(params), getById(id), create(dto), update(id, dto), delete(id), executeFunction(name, params)
  - Handles ApiResponse<T> unwrapping
  - Pagination params support (page, size, sort, search)
  - Each feature service extends this base

- [x] 5.10 Implement login page
  - `LoginComponent` — standalone, form with username + password
  - Call AuthService.login, redirect to dashboard on success
  - Show "Credenciales incorrectas" on 401
  - Dark/light theme support, Pro Arte logo/branding
  - Animated button for submit

### Phase 6: Frontend Feature Modules

- [x] 6.1 Implement Dashboard feature
  - `DashboardComponent` — standalone, lazy loaded
  - Summary cards (total leads, cotizaciones pendientes, eventos proximos)
  - Quick access links to main modules
  - Responsive grid layout, dark/light theme

- [x] 6.2 Implement Usuarios feature (Requirement 1)
  - `UsuarioListComponent` — DataTable with users, roles, status
  - `UsuarioFormComponent` — create/edit form with role selection
  - `PermisoEditorComponent` — JSON permission editor per role (tablas + contexto config)
  - `UsuarioService` extending BaseCrudService
  - Routes: /usuarios, /usuarios/nuevo, /usuarios/:id/editar, /usuarios/roles/:id/permisos

- [x] 6.3 Implement Leads feature (Requirement 2 - Leads)
  - `LeadListComponent` — DataTable with filters by estado
  - `LeadFormComponent` — create/edit form, associate persona/empresa
  - `LeadChartComponent` — pie chart showing distribution by estado
  - `LeadService` extending BaseCrudService + estadisticas endpoint
  - Routes: /leads, /leads/nuevo, /leads/:id/editar

- [ ] 6.4 Implement Personas feature (Requirement 2 - Personas)
  - `PersonaListComponent` — DataTable with search by nombre, documento
  - `PersonaFormComponent` — create/edit form, assign role (contacto/cliente/proveedor)
  - `PersonaDetailComponent` — detail view with context (empresas, leads, cotizaciones)
  - `PersonaService` extending BaseCrudService + asociar-empresa + asignar-rol
  - Routes: /personas, /personas/nuevo, /personas/:id/editar

- [ ] 6.5 Implement Empresas feature (Requirement 2 - Empresas)
  - `EmpresaListComponent` — DataTable with search by razon_social, nit
  - `EmpresaFormComponent` — create/edit form, assign role
  - `EmpresaDetailComponent` — detail view with context (personas asociadas)
  - `EmpresaService` extending BaseCrudService + asignar-rol
  - Routes: /empresas, /empresas/nuevo, /empresas/:id/editar

- [ ] 6.6 Implement Proveedores feature (Requirement 3)
  - `ProveedorListComponent` — DataTable with filters
  - `ProveedorFormComponent` — create/edit, link to persona or empresa
  - `PortafolioComponent` — manage servicios associated to proveedor
  - `SolicitudListComponent` — list of solicitudes with estado
  - `SolicitudFormComponent` — create/edit solicitud
  - `ProveedorService` extending BaseCrudService + portafolio + solicitudes
  - Routes: /proveedores, /proveedores/nuevo, /proveedores/:id/editar, /proveedores/:id/portafolio

- [ ] 6.7 Implement Servicios feature (Requirement 4)
  - `ServicioListComponent` — DataTable with filter by categoria (propio/tercero)
  - `ServicioFormComponent` — create/edit, select parent servicio (hierarchy)
  - `SubservicioTreeComponent` — tree view of servicio hierarchy
  - `PorcentajeListComponent` — DataTable of descuentos/recargos
  - `PorcentajeFormComponent` — create/edit porcentaje
  - `ServicioService`, `PorcentajeService` extending BaseCrudService
  - Routes: /servicios, /servicios/nuevo, /servicios/:id, /descuentos-recargos

- [ ] 6.8 Implement Cotizaciones feature (Requirement 5)
  - `CotizacionListComponent` — DataTable with filters by estado, cliente, fecha, vencimiento
  - `CotizacionFormComponent` — create/edit with items, apply porcentajes, assign cliente
  - `CotizacionItemsComponent` — inline table to add/remove/edit items
  - `CotizacionEstadoComponent` — change estado with transition validation
  - `CotizacionVencimientoComponent` — list cotizaciones proximas a vencer
  - `CotizacionService` extending BaseCrudService + estados + vencimientos + pdf download
  - Routes: /cotizaciones, /cotizaciones/nuevo, /cotizaciones/:id/editar, /cotizaciones/vencimientos

- [ ] 6.9 Implement Eventos feature (Requirement 6)
  - `EventoListComponent` — DataTable of eventos
  - `EventoDetailComponent` — full detail with tabs: proveedores, servicios, personas, observaciones, insumos, alimentacion
  - `EventoProveedoresComponent` — associate proveedores to evento
  - `EventoPersonasComponent` — associate personas with roles (promotor, contacto, coordinador, personal)
  - `EventoObservacionesComponent` — list + create/edit observaciones
  - `EventoService` extending BaseCrudService + crear-desde-cotizacion + sub-associations
  - Routes: /eventos, /eventos/:id

- [ ] 6.10 Implement Personal de Evento feature (Requirement 10)
  - `PersonalListComponent` — DataTable of personal assigned to evento
  - `PersonalFormComponent` — assign persona, proveedor, servicio, turno
  - Automatic valor_turno calculation display (from backend fn)
  - ARL/OP status indicators with visual warnings (red icon if missing)
  - Observaciones per employee field
  - `PersonalEventoService` extending BaseCrudService
  - Routes: /eventos/:id/personal

- [ ] 6.11 Implement Ordenes de Compra feature (Requirement 7)
  - `OrdenCompraListComponent` — DataTable with filters
  - `OrdenCompraFormComponent` — create/edit, link to solicitud
  - Excel download button (mass download)
  - `OrdenCompraService` extending BaseCrudService + descargar-excel
  - Routes: /ordenes-compra, /ordenes-compra/nuevo, /ordenes-compra/:id/editar

- [ ] 6.12 Implement Mensajes feature (Requirement 8)
  - `MensajeListComponent` — DataTable of message templates
  - `MensajeFormComponent` — create/edit message template (rich text or plain)
  - `MensajeService` extending BaseCrudService
  - Routes: /mensajes, /mensajes/nuevo, /mensajes/:id/editar

- [ ] 6.13 Implement Presentaciones feature (Requirement 9)
  - `PresentacionListComponent` — DataTable with filters
  - `PresentacionFormComponent` — create/edit, link to servicio
  - PDF generation/download button
  - `PresentacionService` extending BaseCrudService + pdf
  - Routes: /presentaciones, /presentaciones/nuevo, /presentaciones/:id/editar

- [ ] 6.14 Implement Inventario feature (Requirement 11)
  - `InventarioStockComponent` — current stock table with visual indicators (agotado)
  - `IngresoListComponent` — history of ingresos
  - `IngresoFormComponent` — register new ingreso (cantidad, detalle, fecha)
  - `RetiroListComponent` — history of retiros
  - `RetiroFormComponent` — register new retiro (validates stock via backend)
  - Error display: "No hay suficiente stock para este retiro"
  - `InventarioService` extending BaseCrudService
  - Routes: /inventario, /inventario/ingresos, /inventario/retiros

- [ ] 6.15 Implement Alimentacion feature (Requirement 12)
  - `AlimentacionListComponent` — current status per evento
  - `AlimentacionIngresoFormComponent` — register ingreso
  - `AlimentacionRetiroFormComponent` — register retiro
  - Error display: "No hay suficiente cantidad para este retiro"
  - `AlimentacionService` extending BaseCrudService
  - Routes: /eventos/:id/alimentacion

- [ ] 6.16 Implement Catalogos feature (Requirement 13)
  - `CatalogoListComponent` — DataTable generica para listar valores de un catalogo seleccionado
  - `CatalogoFormComponent` — crear/editar valor de catalogo (nombre, contexto para estados)
  - `CatalogoSelectorComponent` — componente shared dropdown que carga opciones desde /api/v1/catalogos/{tipo}
  - `CatalogoService` extending BaseCrudService — GET, POST, PUT, DELETE por tipo de catalogo
  - Selector de tipo de catalogo: tipo-documento, rol-entidad, estado, categoria-servicio, unidad-medida, rol-evento
  - Validacion visual: advertencia antes de eliminar si el valor esta en uso
  - Routes: /catalogos, /catalogos/:tipo

### Phase 7: Integration and Testing

- [ ] 7.1 Backend unit tests for services layer
  - JUnit 5 + Mockito tests for each service class
  - Test CRUD operations, permission checks, edge cases
  - Test ProcedureExecutorService with mocked JDBC
  - Test AuthService token generation/validation
  - Minimum 80% coverage on services

- [ ] 7.2 Backend integration tests with Testcontainers
  - PostgreSQL Testcontainer configuration
  - Test Flyway migrations run correctly
  - Test repository queries with real database
  - Test triggers and functions (fn_recalcular_total, trg_actualizar_stock)
  - Test fn_crear_evento_desde_cotizacion validates estado=APROBADA
  - Test soft-delete behavior (records hidden after deactivation)

- [ ] 7.3 Backend controller tests with MockMvc
  - Test all endpoints return correct HTTP status codes
  - Test authentication required (401 for unauthenticated)
  - Test permission denied (403 for unauthorized)
  - Test standard JSON response format
  - Test pagination parameters work correctly
  - Minimum 60% coverage on controllers

- [ ] 7.4 Frontend unit tests for services
  - Jasmine tests for AuthService, PermissionService, ErrorMessageService, ThemeService
  - Test BaseCrudService generic methods
  - Test interceptors (JWT attachment, error handling)
  - Test guards (auth redirect, permission check)
  - Minimum 70% coverage on services

- [ ] 7.5 Frontend component tests
  - Test DataTableComponent renders columns, handles actions
  - Test ConfirmDialogComponent emits correct events
  - Test LoginComponent form validation and error display
  - Test theme switching works (dark/light)
  - Test permission-based UI element visibility
  - Minimum 50% coverage on components

- [ ] 7.6 End-to-end integration validation
  - Verify full login → dashboard → CRUD flow for each module
  - Verify permission enforcement (backend blocks + frontend hides)
  - Verify soft-delete (records disappear from lists but exist in DB)
  - Verify PostgreSQL functions execute correctly through API
  - Verify PDF/Excel download endpoints work
  - Verify stock validation errors display correctly
  - Document any manual test steps in a checklist

## Notes

- Las fases 1-3 son prerequisitos estrictos: no se puede iniciar backend modules sin la capa comun ni el schema de DB.
- Las fases frontend (5-6) pueden avanzar en paralelo con backend (3-4) una vez completado el setup (Phase 1).
- Phase 7 (testing) requiere que ambas capas (frontend + backend) esten funcionales.
- Todas las migraciones Flyway deben ser idempotentes y ejecutarse en orden estricto (V1 a V10).
- La migracion V10 normaliza campos VARCHAR (tipo_documento, rol, estado, categoria, unidad_medida, rol_evento) a UUID FK references hacia tablas de catalogo.
- Los modulos backend son independientes entre si dentro de Phase 4, excepto Eventos (4.8) que depende de Cotizaciones (4.7).
- El frontend sigue el patron: cada feature module tiene list + form + service + model, reutilizando DataTable y DetailView compartidos.
- La logica de negocio (calculos, validaciones de stock, creacion condicional de eventos) reside exclusivamente en PostgreSQL.
- Convenciones: camelCase en codigo, UUID para IDs, soft-delete universal, mensajes en espanol, dark/light theme obligatorio.
