---
inclusion: manual
---

# Problem Details — RFC 9457

Use when implementing error handling with RFC 9457 standard. Uses Spring's built-in ProblemDetail.

## Enable

```yaml
spring:
  mvc:
    problemdetails:
      enabled: true
```

## Response Shape

```json
{
  "type": "https://api.example.com/errors/order-not-found",
  "title": "Order Not Found",
  "status": 404,
  "detail": "No order found with id: 550e8400-e29b-41d4-a716-446655440000",
  "instance": "/api/v1/orders/550e8400-e29b-41d4-a716-446655440000",
  "errorCode": "ORDER_NOT_FOUND",
  "timestamp": "2026-04-13T10:00:00Z"
}
```

## Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create("https://api.example.com/errors/not-found"));
        problem.setTitle("Resource Not Found");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", "NOT_FOUND");
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpHeaders headers,
        HttpStatusCode status, WebRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setType(URI.create("https://api.example.com/errors/validation"));
        problem.setTitle("Validation Failed");
        problem.setProperty("violations", ex.getBindingResult().getFieldErrors().stream()
            .map(e -> Map.of("field", e.getField(), "message", e.getDefaultMessage()))
            .toList());
        return ResponseEntity.badRequest().body(problem);
    }
}
```

## Custom Domain Exceptions

```java
public abstract class DomainException extends RuntimeException {
    private final String errorCode;
    protected DomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

public class OrderNotFoundException extends DomainException {
    public OrderNotFoundException(UUID orderId) {
        super("ORDER_NOT_FOUND", "Order not found: " + orderId);
    }
}
```

## ProblemDetail vs ApiResponse

Choose one approach per project — don't mix them.

| Use Case | Approach |
|---|---|
| Public API with diverse clients | ProblemDetail (RFC standard) |
| Internal microservices | Either — be consistent |

## Gotchas
- Agent returns `Map<String, Object>` for errors — use `ProblemDetail`
- Agent exposes raw exception messages in 500s — log and return generic message
- Agent uses custom envelope alongside ProblemDetail — pick one standard
- Agent forgets `type` URI — required for RFC 9457 compliance
- Agent doesn't extend `ResponseEntityExceptionHandler` — extend it for free MVC exception handling
