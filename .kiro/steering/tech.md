# Technical Document: ERP Pro Arte

## 1. Stack Tecnológico

| Capa | Tecnología | Versión | Propósito / Responsabilidad |
| :--- | :--- | :--- | :--- |
| **Backend Framework** | Spring Boot | 3.3.5 | API REST, orquestación, seguridad y transporte de datos |
| **Lenguaje Backend** | Java (OpenJDK) | 21 | Lenguaje tipado con soporte para Records, Pattern Matching y Virtual Threads |
| **Persistencia / ORM** | Spring Data JPA / Hibernate | 6.5+ | Mapeo objeto-relacional y repositorios CRUD |
| **Seguridad** | Spring Security + JJWT | 0.12.6 | Autenticación basada en tokens JWT sin estado (Stateless) |
| **Motor de Base de Datos** | PostgreSQL | 15+ | Almacenamiento relacional, triggers y funciones PL/pgSQL |
| **Migraciones de DB** | Flyway Core + PostgreSQL | 10.x | Versionamiento y ejecución secuencial de esquemas SQL (V1 a V10) |
| **Frontend Framework** | Angular | 18+ | Single Page Application (SPA) con Standalone Components y Signals |
| **Estilos y UI** | SCSS / CSS Custom Properties | - | Sistema de temas dinámico (Dark/Light mode) con paleta corporativa |
| **Animaciones UI** | `@angular/animations` | 18+ | Micro-interacciones en botones, transiciones de rutas y modales |
| **Generación de Reportes** | LibrePDF / Apache POI | 2.0.3 / 5.2.5 | Exportación de cotizaciones y presentaciones en PDF, y órdenes en Excel (.xlsx) |
| **Documentación API** | SpringDoc OpenAPI | 2.6.0 | Swagger UI interactivo en `/swagger-ui.html` |
| **Contenedores** | Docker / Docker Compose | 3.8+ | Orquestación local de PostgreSQL y servicios |

---

## 2. Arquitectura del Sistema

El sistema implementa una arquitectura en tres capas (Three-Tier Architecture):

```
┌────────────────────────────────────────────────────────┐
│                   Angular 18 SPA                       │
│  (Standalone Components, Signals, Reactive Forms, SCSS)│
└──────────────────────────┬─────────────────────────────┘
                           │ HTTP / JSON (REST API)
┌──────────────────────────▼─────────────────────────────┐
│                 Spring Boot 3.3 Backend                │
│  ├── Controllers (Endpoints REST & Validación DTO)     │
│  ├── Security (Filtro JWT & PermissionEvaluator)       │
│  ├── Services (Coordinación y Generación Documental)   │
│  └── Repositories (Spring Data JPA + Soft Delete)      │
└──────────────────────────┬─────────────────────────────┘
                           │ JDBC / HikariCP Pool
┌──────────────────────────▼─────────────────────────────┐
│                 PostgreSQL 15+ Database                │
│  ├── Tablas Normalizadas + Lookups (UUID PK/FK)        │
│  ├── Triggers (Cálculo de stock, subtotales de ítems)  │
│  └── Funciones / Procedures PL/pgSQL (Lógica pesada)   │
└────────────────────────────────────────────────────────┘
```

---

## 3. Patrones de Diseño Implementados

1.  **Eliminación Lógica (`SoftDeleteRepository`):** Todas las entidades extienden de `BaseEntity` y usan la anotación `@SQLRestriction("activo = true")` para filtrar registros inactivos de manera transparente en consultas estándar.
2.  **Auditoría Automática (`AuditingEntityListener`):** Integración con `AuditorAwareImpl` para inyectar automáticamente el UUID del usuario autenticado en `created_by`, junto con `created_at` y `updated_at`.
3.  **Envoltorio de Respuestas (`ApiResponse<T>`):** Formato homogéneo para todas las respuestas REST:
    ```json
    { "success": true, "data": { ... }, "message": "Operación exitosa" }
    ```
4.  **Manejo Global de Excepciones (`GlobalExceptionHandler`):** Captura centralizada de excepciones (`ResourceNotFoundException`, `BusinessException`, `InsufficientStockException`) mapeadas a códigos de error comprensibles en español.
5.  **Control de Acceso basado en Permisos JSONB:** Matriz granular almacenada en la tabla `permiso` que determina accesos por tabla y visualización de contextos relacionales.

---

## 4. Análisis Crítico: Incongruencias y Puntos de Falla según Estándares de Spring Boot

Al contrastar la arquitectura actual con la documentación oficial de **Spring Boot 3.3**, **Spring Data JPA / Hibernate 6** y las mejores prácticas de la industria, se identifican las siguientes incongruencias estructurales y riesgos operativos:

### Incongruencia 1: Conflicto entre Caché de Primer Nivel (JPA) y Triggers/Funciones de PostgreSQL (Stale Cache & Overwrites)
*   **Mecanismo actual:** Spring Boot delega cálculos clave a funciones y triggers de PostgreSQL (ej. `fn_recalcular_total_cotizacion`, `trg_actualizar_stock`). Cuando se guardan ítems de cotización, se invoca un procedimiento en base de datos vía `JdbcTemplate` que actualiza la columna `total` directamente en la tabla `cotizacion`.
*   **Por qué falla según Spring/JPA:**
    1.  Hibernate mantiene una caché en memoria (*Persistence Context*) con el estado de la entidad `Cotizacion` cargada.
    2.  Cuando `JdbcTemplate` o un trigger modifica filas en la base de datos por debajo de Hibernate, el *Persistence Context* **no se entera del cambio**.
    3.  Llamar a `repository.findById(id)` dentro de la misma transacción simplemente devuelve el objeto en caché sin consultar la base de datos.
    4.  Al finalizar la transacción (`@Transactional`), el mecanismo de *dirty-checking* de Hibernate puede hacer un `flush` de su entidad en memoria, **sobrescribiendo el total calculado por PostgreSQL con el valor antiguo (cero o desactualizado)**.
*   **Solución oficial:** Se debe invocar `entityManager.refresh(entity)` o `entityManager.clear()` explícitamente tras operaciones JDBC nativas, o unificar la lógica de cálculo en la capa de servicio de Spring Boot.

---

### Incongruencia 2: Consultas Bulk en `SoftDeleteRepository` sin `clearAutomatically = true`
*   **Mecanismo actual:** `SoftDeleteRepository` utiliza `@Modifying @Query("UPDATE #{#entityName} e SET e.activo = false...")`.
*   **Por qué falla según Spring Data JPA:**
    1.  Las consultas de modificación masiva JPQL (`UPDATE`/`DELETE`) se traducen a SQL directo y **omiten el ciclo de vida de entidades de JPA**.
    2.  Sin la propiedad `@Modifying(clearAutomatically = true, flushAutomatically = true)`, las entidades que ya estaban cargadas en la sesión de Hibernate permanecen en memoria con `activo = true`.
    3.  Cualquier operación posterior en el mismo hilo de ejecución operará sobre datos zombis que ya fueron "eliminados" en base de datos.

---

### Incongruencia 3: Desajuste de Tipos en `ProcedureExecutorService` (Casteo JSONB vs. Firmas PL/pgSQL)
*   **Mecanismo actual:** En `ProcedureExecutorService.java`, las llamadas genéricas se construyen como:
    ```java
    String sql = "SELECT " + functionName + "(?::jsonb)";
    ```
*   **Por qué falla según PostgreSQL y Spring JDBC:**
    1.  En la migración `V8__functions_triggers.sql`, funciones como `fn_recalcular_total_cotizacion(p_cotizacion_id UUID)` y `fn_crear_evento_desde_cotizacion(p_cotizacion_id UUID)` esperan un parámetro de tipo **`UUID` escalar**, no un objeto `JSONB`.
    2.  Invocar el endpoint genérico `/api/v1/cotizaciones/execute/fn_recalcular_total_cotizacion` produce un error inmediato de PostgreSQL: `function fn_recalcular_total_cotizacion(jsonb) does not exist`.

---

### Incongruencia 4: Validación Imperativa de Permisos vs. Seguridad Declarativa `@PreAuthorize`
*   **Mecanismo actual:** Los controladores validan permisos manualmente dentro de cada método llamando a `validatePermission("crear")`.
*   **Por qué es una mala práctica:**
    1.  Si un desarrollador olvida agregar `validatePermission(...)` al crear un nuevo método en un controlador, el endpoint queda **completamente expuesto** a cualquier usuario con token válido.
    2.  Viola el principio DRY (Don't Repeat Yourself).
*   **Solución oficial Spring Security:** Utilizar seguridad declarativa con anotaciones estándar `@PreAuthorize("@permissionEvaluator.hasPermission('usuarios', 'crear')")` habilitadas mediante `@EnableMethodSecurity`.

---

### Incongruencia 5: Transacciones Abortadas de PostgreSQL por `RAISE EXCEPTION` en Triggers
*   **Mecanismo actual:** En `trg_fn_actualizar_stock`, si el stock es insuficiente, se lanza `RAISE EXCEPTION`. En `InventarioService`, se intenta capturar `DataIntegrityViolationException`.
*   **Por qué falla:**
    1.  Cuando PostgreSQL lanza una excepción dentro de un trigger en un bloque transaccional, **marca la transacción completa como abortada** (`current transaction is aborted, commands ignored until end of transaction block`).
    2.  Cualquier intento posterior de Spring de registrar auditorías, consultar datos de respaldo o realizar limpieza dentro de la misma conexión falla de forma irrecuperable.
    3.  Además, si Hibernate pospone el `flush` hasta el commit (fuera del bloque `try-catch` del servicio), la excepción no es capturada por el método de negocio, provocando errores HTTP 500 genéricos en vez del error de negocio esperado (HTTP 400).

---

### Incongruencia 6: Mapeo Híbrido en Entidades (`rolId` primitivo vs. `Rol rol` Relación)
*   **Mecanismo actual:** En entidades como `Usuario.java`:
    ```java
    @Column(name = "rol_id", nullable = false)
    private UUID rolId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", insertable = false, updatable = false)
    private Rol rol;
    ```
*   **Por qué es riesgoso:**
    1.  Mantiene dos fuentes de verdad para la misma columna en el modelo de objetos.
    2.  Si el código modifica `usuario.setRolId(nuevoUuid)`, la referencia `usuario.getRol()` sigue apuntando al rol anterior en la sesión de Hibernate, generando desincronización y posibles `NullPointerException` si la relación no fue inicializada.

---

## 5. Recomendaciones de Evolución y Refactorización

1.  **Unificar la Lógica de Negocio en Spring Boot:** Migrar cálculos aritméticos y validaciones de stock a los servicios de Java (`@Service`), dejando a PostgreSQL enfocado en consistencia relacional e indexación.
2.  **Reforzar `SoftDeleteRepository`:** Añadir `clearAutomatically = true` a todas las anotaciones `@Modifying`.
3.  **Adoptar `@PreAuthorize`:** Reemplazar las llamadas manuales `validatePermission` por interceptores declarativos de Spring Security.
4.  **Caché de Permisos:** Implementar Spring Cache (Caffeine o Redis) para no re-consultar la base de datos en cada petición HTTP autenticada.
