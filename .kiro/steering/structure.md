# Structure Document: ERP Pro Arte

## 1. Estructura General del Repositorio

```
PRO ARTE/
├── .kiro/                       # Contexto documental, specs, steering y hooks
│   ├── specs/                   # Requerimientos, diseño y tareas del ERP
│   │   └── erp-pro-arte/        # requirements.md, design.md, tasks.md
│   ├── steering/                # Guías de dirección de IA (product, tech, structure)
│   └── settings/                # Configuración de herramientas
├── backend/                     # API REST Spring Boot 3.3 (Java 21)
│   ├── src/main/java/com/proarte/erp/
│   ├── src/main/resources/
│   │   ├── application.yml      # Perfiles de configuración (dev, test, prod)
│   │   └── db/migration/        # Scripts de migración SQL Flyway (V1 a V10)
│   ├── pom.xml                  # Configuración de dependencias Maven
│   └── Dockerfile.dev           # Imagen de desarrollo backend
├── frontend/                    # Single Page Application Angular 18
│   ├── src/app/
│   │   ├── core/                # Servicios singleton, guards e interceptores
│   │   ├── shared/              # Componentes, directivas y animaciones reutilizables
│   │   └── features/            # Módulos de funcionalidad (Lazy Loading)
│   ├── package.json             # Dependencias npm
│   └── angular.json             # Configuración del CLI de Angular
├── docs/                        # Diagramas UML y modelos conceptuales
├── docker-compose.yml           # Orquestación de base de datos PostgreSQL local
├── product.md                   # Documento de dirección de producto
├── tech.md                      # Documento de dirección técnica y arquitectura
└── structure.md                 # Documento de estructura del proyecto
```

---

## 2. Arquitectura de Paquetes Backend (`backend/src/main/java/com/proarte/erp/`)

El backend sigue una organización modular por dominio de negocio:

```
com.proarte.erp/
├── ErpProArteApplication.java   # Clase principal con @SpringBootApplication
├── auth/                        # Autenticación y credenciales
│   ├── controller/              # AuthController (/api/v1/auth)
│   ├── dto/                     # LoginRequest, AuthResponse, RefreshTokenRequest
│   ├── entity/                  # Usuario, Rol, Permiso
│   ├── repository/              # UsuarioRepository, RolRepository, PermisoRepository
│   └── service/                 # AuthService, UserDetailsServiceImpl
├── security/                    # Configuración de Spring Security y JWT
│   ├── SecurityConfig.java      # Configuración de filtros, CORS, CSRF y rutas públicas
│   ├── JwtTokenProvider.java    # Generación y validación de tokens JWT
│   ├── JwtAuthenticationFilter.java # Filtro extractor de Bearer Token
│   ├── PermissionEvaluator.java # Validador en memoria de permisos JSONB
│   └── CustomUserDetails.java   # Adaptador de usuario para Spring Security
├── common/                      # Infraestructura y clases base transversales
│   ├── entity/BaseEntity.java   # Clase abstracta: id UUID, activo, created_at, created_by
│   ├── repository/SoftDeleteRepository.java # Extensión JPA para eliminación lógica
│   ├── service/ProcedureExecutorService.java # Ejecutor de funciones PostgreSQL
│   ├── controller/ProcedureController.java # Endpoint genérico /execute/{function}
│   └── dto/                     # ApiResponse, PageResponse, ProcedureRequest
├── exception/                   # Manejo global de errores
│   ├── GlobalExceptionHandler.java # @RestControllerAdvice para respuestas estándar
│   ├── BusinessException.java   # Excepción de lógica de negocio (HTTP 400)
│   ├── ResourceNotFoundException.java # Excepción de recurso no encontrado (HTTP 404)
│   └── InsufficientStockException.java # Excepción de stock agotado (HTTP 400)
├── config/                      # Configuraciones de Spring (Auditoría, CORS, OpenAPI)
│   ├── AuditingConfig.java      # @EnableJpaAuditing y AuditorAware
│   └── CorsProperties.java      # Mapeo de orígenes permitidos
├── catalogos/                   # Tablas lookup maestras (Normalización)
│   ├── entity/                  # TipoDocumento, RolEntidad, Estado, CategoriaServicio, etc.
│   ├── repository/              # Repositorios JPA para cada catálogo
│   ├── service/CatalogoService.java # CRUD genérico y validación de FKs en uso
│   └── controller/CatalogoController.java # /api/v1/catalogos/{tipo}
├── leads/                       # Oportunidades comerciales
├── personas/                    # Directorio de personas / contactos
├── empresas/                    # Clientes y proveedores corporativos
├── proveedores/                 # Proveedores, portafolios y solicitudes de servicio
├── servicios/                   # Catálogo de servicios, jerarquías y recargos/descuentos
├── cotizaciones/                # Cotizaciones, ítems de cotización y exportación PDF
├── eventos/                     # Eventos operativos, minutas, contactos y proveedores
├── ordenes/                     # Órdenes de compra y exportación Excel
├── mensajes/                    # Plantillas de mensajería comercial
├── presentaciones/              # Presentaciones comerciales de servicios
├── inventario/                  # Control de stock, ingresos y retiros de insumos
└── alimentacion/                # Planificación de catering por evento
```

---

## 3. Arquitectura del Frontend (`frontend/src/app/`)

El frontend está estructurado bajo los principios modernos de Angular 18 (Standalone Components, Signals y Lazy Loading):

```
src/app/
├── core/
│   ├── guards/
│   │   ├── auth.guard.ts        # Redirección al login si no hay token activo
│   │   └── permission.guard.ts  # Bloqueo de rutas según matriz JSON de permisos
│   ├── interceptors/
│   │   ├── jwt.interceptor.ts   # Inyección automática de header 'Authorization: Bearer'
│   │   └── error.interceptor.ts # Captura global de errores HTTP y mensajes en español
│   └── services/
│       ├── auth.service.ts      # Manejo de sesión y almacenamiento de tokens
│       ├── theme.service.ts     # Alternancia de tema Dark/Light persistido en localStorage
│       └── error-message.service.ts # Diccionario centralizado de errores
├── shared/
│   ├── components/
│   │   ├── data-table/          # Tabla paginada reutilizable con acciones por permisos
│   │   ├── confirm-dialog/      # Modal de confirmación para eliminaciones lógicas
│   │   ├── animated-button/     # Botones con micro-interacciones
│   │   ├── search-filter/       # Input de búsqueda con debounce de 300ms
│   │   └── detail-view/         # Vista detalle con contexto relacional condicional
│   ├── animations/              # Disparadores de animación de Angular
│   └── services/
│       └── base-crud.service.ts # Clase base genérica para llamados HTTP CRUD
├── features/                    # Módulos perezosos (Lazy Loaded)
│   ├── auth/                    # Pantalla de login
│   ├── dashboard/               # Métricas generales y accesos rápidos
│   ├── usuarios/                # Gestión de usuarios y editor visual de permisos JSON
│   ├── leads/                   # Listado, formulario y gráfico de estados
│   ├── personas/ & empresas/    # Directorios y asignación de roles
│   ├── proveedores/             # Proveedor, portafolio y solicitudes
│   ├── servicios/               # Árbol de servicios y descuentos
│   ├── cotizaciones/            # Creador de cotizaciones, cálculo y descarga PDF
│   ├── eventos/                 # Coordinación de eventos, asignación de personal
│   ├── inventario/              # Kardex, ingresos/salidas y alerta de stock
│   ├── alimentacion/            # Control de raciones por evento
│   ├── ordenes-compra/          # Listado y descarga masiva en Excel
│   └── catalogos/               # Mantenimiento de tablas lookup
├── app.config.ts                # Configuración de proveedores (HttpClient, Router, Animations)
├── app.routes.ts                # Definición de rutas con carga perezosa (loadChildren / loadComponent)
└── app.html / app.scss          # Shell de la aplicación con barra superior y navegación
```

---

## 4. Esquema de Base de Datos y Migraciones Flyway

| Migración | Nombre | Contenido Principal |
| :--- | :--- | :--- |
| **`V1`** | `base_schema.sql` | Tablas base: `rol`, `permiso` (JSONB), `usuario`. Habilitación de `gen_random_uuid()`. |
| **`V2`** | `personas_empresas.sql` | Tablas: `persona`, `empresa`, `persona_empresa`, `lead`. |
| **`V3`** | `proveedores_servicios.sql` | Tablas: `proveedor`, `servicio` (jerárquico), `portafolio`, `solicitud_servicio`. |
| **`V4`** | `descuentos_recargos.sql` | Tablas: `tipo_descuento_recargo`, `descuento_recargo`. |
| **`V5`** | `cotizaciones.sql` | Tablas: `cotizacion`, `cotizacion_item`. |
| **`V6`** | `eventos.sql` | Tablas: `evento`, `evento_contacto`, `evento_proveedor`, `evento_personal`, `evento_observacion`. |
| **`V7`** | `ordenes_inventario.sql` | Tablas: `orden_compra`, `mensaje`, `presentacion`, `insumo`, `insumo_movimiento`, `evento_insumo`, `evento_alimentacion`. |
| **`V8`** | `functions_triggers.sql` | Funciones PL/pgSQL (`fn_recalcular_total_cotizacion`, `fn_crear_evento_desde_cotizacion`, `fn_calcular_valor_turno`) y triggers de stock/subtotales. |
| **`V9`** | `seed_data.sql` | Datos iniciales de roles (`ADMIN`, `COMERCIAL`, `OPERATIVO`, `COORDINADOR`), usuario admin y permisos predeterminados. |
| **`V10`** | `normalizacion_lookup_tables.sql` | Tablas lookup: `tipo_documento`, `rol_entidad`, `estado`, `categoria_servicio`, `unidad_medida`, `rol_evento` y migración de columnas VARCHAR a FKs UUID. |
