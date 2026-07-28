# Spring Boot Project — Convenciones Generales

Este proyecto utiliza Spring Boot 3.x con las siguientes convenciones y stack tecnológico.

## Stack

- **Java**: 21
- **Spring Boot**: 3.3+
- **Build**: Maven
- **Base de datos**: PostgreSQL
- **Migraciones**: Flyway
- **Testing**: JUnit 5 + Mockito + AssertJ + Testcontainers
- **Seguridad**: Spring Security con JWT
- **Documentación API**: OpenAPI 3.0

## Convenciones Generales

### Inyección de dependencias
- Constructor injection SIEMPRE — nunca `@Autowired` field injection
- Usa `@RequiredArgsConstructor` de Lombok para eliminar boilerplate

### Identificadores
- Usa `UUID` para todos los IDs expuestos en API
- Nunca expongas auto-increment integers en URLs

### DTOs
- Records de Java para DTOs inmutables
- Request y Response DTOs separados — nunca reutilices la misma clase
- Mapeo vía static factory: `ResponseDto.from(entity)`

### Naming
- Packages: `com.example.{module}.{layer}` (e.g., `com.example.orders.service`)
- Controllers: `{Entity}Controller`
- Services: `{Entity}Service`
- Repositories: `{Entity}Repository`
- DTOs: `Create{Entity}Request`, `Update{Entity}Request`, `{Entity}Response`

### Logging
- Usa `@Slf4j` — nunca `System.out.println`
- No loggees datos sensibles (passwords, tokens, PII)

### Error Handling
- Un solo `@RestControllerAdvice` global
- Nunca try/catch en controllers
- Excepciones custom por dominio

### Configuración
- Secretos via variables de entorno: `${SECRET_NAME}`
- `@ConfigurationProperties` para grupos de configuración
- Perfiles: `dev`, `test`, `prod`

## Comandos

```bash
# Build
mvn clean package -DskipTests

# Tests
mvn test

# Run local
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Docker build
docker build -t app-name .
```

## Steering Files Disponibles (Manual)

Los siguientes steering se activan manualmente con `#` en el chat:

- `spring-boot-mcp-server` — Para construir servidores MCP con Java SDK
- `spring-boot-spring-ai` — Para integrar LLMs y RAG con Spring AI
- `spring-boot-domain-driven-design` — Para DDD, aggregates, value objects
- `spring-boot-redis-caching` — Para Redis y caching
- `spring-boot-batch` — Para Spring Batch jobs y ETL
- `spring-boot-openapi-first` — Para desarrollo API-first con OpenAPI
- `spring-boot-problem-details` — Para error handling RFC 9457
- `spring-boot-oauth2-resource-server` — Para OAuth2 con proveedores externos
- `spring-boot-hateoas` — Para hypermedia links en REST
- `spring-boot-ai-observability` — Para métricas y monitoreo de LLMs
