# Guía y Mejores Prácticas para el Desarrollo en Spring Boot

Este documento sirve como referencia (`agents.md`) para establecer los lineamientos de codificación, arquitectura y gestión del proyecto **ERP Pro Arte**. Está basado en el stack tecnológico definido (Spring Boot 3.3, Java 21, Hibernate 6.5) y aborda las consideraciones específicas de la arquitectura actual.

---

## 1. Enlaces de Documentación Oficial

De acuerdo con el stack del proyecto (`tech.md`), aquí están los enlaces a la documentación oficial de las versiones utilizadas:

*   **Java (OpenJDK) 21:** [Java 21 Documentation](https://docs.oracle.com/en/java/javase/21/docs/api/) | [Guía de Records](https://docs.oracle.com/en/java/javase/21/language/records.html)
*   **Spring Boot 3.3.x:** [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/3.3.x/reference/html/)
*   **Spring Data JPA:** [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/)
*   **Hibernate ORM 6.5:** [Hibernate 6.5 User Guide](https://docs.jboss.org/hibernate/orm/6.5/userguide/html_single/Hibernate_User_Guide.html)
*   **Spring Security:** [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
*   **SpringDoc OpenAPI (Swagger):** [SpringDoc v2](https://springdoc.org/)
*   **PostgreSQL 15:** [PostgreSQL 15 Documentation](https://www.postgresql.org/docs/15/index.html)
*   **Flyway:** [Flyway Documentation](https://documentation.red-gate.com/fd)
*   **JJWT (Java JWT):** [JJWT GitHub Repository](https://github.com/jwtk/jjwt)

---

## 2. Arquitectura de Software y Capas

Mantener una separación estricta de responsabilidades (Three-Tier Architecture):

1.  **Capa de Controladores (`@RestController`)**:
    *   **Responsabilidad:** Manejar peticiones HTTP, validación de entrada (anotaciones `@Valid`), orquestar las llamadas a los servicios y devolver respuestas estandarizadas.
    *   **Regla de oro:** No debe contener lógica de negocio ni acceso a repositorios directamente.

2.  **Capa de Servicios (`@Service`)**:
    *   **Responsabilidad:** Contener toda la lógica de negocio, reglas de validación complejas y coordinación de múltiples repositorios.
    *   **Transaccionalidad:** Usar `@Transactional` en los métodos que modifican el estado de la base de datos.
    *   **Regla de oro:** La lógica matemática, cálculos complejos y validaciones deben vivir aquí y no delegarse a Triggers/Procedimientos de PostgreSQL a menos que sea estrictamente necesario por rendimiento extremo.

3.  **Capa de Acceso a Datos (`@Repository`)**:
    *   **Responsabilidad:** Interfaces que extienden de `JpaRepository` o consultas JPQL/Nativas.
    *   **Regla de oro:** Solo realizar operaciones de persistencia.

---

## 3. Mejores Prácticas de Codificación (Basado en Análisis Crítico)

Para solucionar las incongruencias detectadas en la arquitectura actual, es obligatorio seguir las siguientes directrices:

### 3.1. Uso de Java 21 y DTOs
*   **Records:** Utilizar `record` de Java 21 para la creación de DTOs (Data Transfer Objects). Son inmutables, limpios y reducen el boilerplate.
*   **Aislamiento:** Nunca devolver ni recibir Entidades JPA (`@Entity`) directamente en los controladores. Siempre mapear `Entity <-> DTO`.

### 3.2. Estandarización de Respuestas y Excepciones
*   **Envoltorio Homogéneo:** Todos los endpoints deben devolver la estructura acordada en `ApiResponse<T>`: `{ "success": true, "data": {...}, "message": "..." }`.
*   **Manejo Global:** Utilizar el `GlobalExceptionHandler` (`@ControllerAdvice`) para capturar excepciones como `EntityNotFoundException` o `BusinessException` y transformarlas en respuestas HTTP adecuadas (404, 400). **No usar bloques try-catch genéricos en los controladores.**

### 3.3. Persistencia y Spring Data JPA
*   **Soft Deletes (Eliminación Lógica):** Al realizar operaciones de borrado masivo vía `@Modifying` o JPQL nativo, **siempre** usar `@Modifying(clearAutomatically = true, flushAutomatically = true)`. Esto evita mantener datos "zombies" en el caché de primer nivel de Hibernate.
*   **Sincronización con PostgreSQL (Triggers):** Evitar que la lógica de la base de datos mute filas que Spring Boot está gestionando en la misma transacción. Si un Procedure/Trigger modifica datos, es obligatorio refrescar la entidad con `entityManager.refresh(entity)` o mover dicha lógica de cálculo a los `@Service` de Java.
*   **Mapeo de Entidades:** **Nunca** realizar un mapeo híbrido en una entidad. Si existe una relación, mapéala solo con el objeto (ej. `@ManyToOne Rol rol`), no dupliques la columna con un atributo primitivo (ej. evitar el uso de `UUID rolId` junto con `Rol rol`).

### 3.4. Seguridad Declarativa
*   **`@PreAuthorize`:** La validación de permisos debe realizarse de forma **declarativa**. Nunca llamar a métodos imperativos como `validatePermission(...)` dentro de los métodos del controlador.
*   **Implementación:** Usar `@PreAuthorize("@permissionEvaluator.hasPermission('recurso', 'accion')")` a nivel de clase o de método en los controladores.

### 3.5. Consultas Nativas y Procedimientos Almacenados
*   **Tipado estricto:** Al llamar a procedimientos desde `JdbcTemplate` o Repositorios, respetar los tipos exactos (ej. pasar `UUID` y no casteos a `JSONB` si la función en PL/pgSQL espera un UUID).
*   **Gestión de Errores de BD:** Si un Trigger de PostgreSQL lanza un `RAISE EXCEPTION`, la transacción entera se abortará. Prefiera realizar estas validaciones de consistencia de negocio (ej. validación de stock insuficiente) en la capa `@Service` de Spring Boot y dejar que la BD solo valide la integridad estructural.

---

## 4. Checklist para Nuevos Endpoints (REST API)

Antes de dar por completada la creación de un nuevo recurso en la API, verificar:

- [ ] ¿El controlador está usando `@PreAuthorize` para asegurar el endpoint?
- [ ] ¿El método del controlador recibe y devuelve DTOs (Records) en lugar de entidades JPA?
- [ ] ¿La respuesta está envuelta en la clase `ApiResponse<T>`?
- [ ] ¿Se usa `@Valid` para las validaciones de entrada en los DTOs?
- [ ] ¿Toda la lógica de negocio vive en un `@Service` y no en el controlador?
- [ ] ¿El método del servicio que altera datos está anotado con `@Transactional`?
- [ ] ¿El mapeo de relaciones JPA usa objetos (`@ManyToOne`, `@OneToMany`) sin duplicar la columna de clave foránea como atributo primitivo?
- [ ] ¿El endpoint está debidamente documentado usando las anotaciones de Swagger (`@Operation`, `@ApiResponse`) de SpringDoc?

---
*Este documento debe ser consultado continuamente por los agentes de IA y los desarrolladores al implementar nuevas funcionalidades en el backend del ERP Pro Arte.*
