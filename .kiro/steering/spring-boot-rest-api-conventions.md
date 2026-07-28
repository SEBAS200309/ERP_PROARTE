---
inclusion: fileMatch
fileMatchPattern: "**/controller/**/*.java,**/api/**/*.java,**/*Controller.java"
---

# REST API Conventions — Spring Boot 3.x

Use when generating REST controllers, response wrappers, DTOs, error handlers, or any
HTTP-facing code. Defines response envelope, HTTP status mapping, pagination, and versioning.

## Response Envelope

All endpoints return a consistent envelope:

```json
{
  "success": true,
  "data": { },
  "error": null,
  "timestamp": "2026-04-13T10:00:00Z"
}
```

## ApiResponse Wrapper

```java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    T data,
    ApiError error,
    Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ApiError(code, message, List.of()), Instant.now());
    }
}

public record ApiError(String code, String message, List<String> details) {}
```

## HTTP Status Mapping

| Scenario | Status |
|----------|--------|
| GET — found | 200 |
| POST — created resource | 201 |
| PUT/PATCH — updated | 200 |
| DELETE — deleted | 204 (no body) |
| Validation failure | 400 |
| Unauthenticated | 401 |
| Forbidden | 403 |
| Not found | 404 |
| Conflict (duplicate) | 409 |
| Unhandled server error | 500 |

## URL Conventions

- **Plural nouns** for resources: `/orders`, `/users`, `/products`
- **Kebab-case** for multi-word: `/order-items`, not `/orderItems`
- **Versioning in path**: `/api/v1/orders`
- **Nested resources** max 2 levels: `/orders/{id}/items` ✅
- **IDs as UUIDs** in path, never auto-increment integers exposed in URL

## Pagination

Query params: `?page=0&size=20&sort=createdAt,desc`

Use Spring Data `Pageable` in controllers. **Cap the page size:**

```yaml
spring:
  data:
    web:
      pageable:
        default-page-size: 20
        max-page-size: 100
```

## Global Exception Handler

```java
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(ApiResponse.error("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
        return ResponseEntity.status(400)
            .body(new ApiResponse<>(false, null, new ApiError("VALIDATION_FAILED", "Invalid input", details), Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        return ResponseEntity.status(500).body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
```

## Gotchas
- Agent returns raw objects without envelope — always wrap in `ApiResponse.ok(...)`
- Agent uses `ResponseEntity<Map<String, Object>>` for errors — use `ApiResponse`
- Agent puts exception handlers in controllers — use `@RestControllerAdvice`
- Agent uses `Long` IDs in URLs — use `UUID`
- Agent accepts unbounded `Pageable` — set max-page-size
- Agent returns `Page<Entity>` directly — map to DTOs first
