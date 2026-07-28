---
inclusion: fileMatch
fileMatchPattern: "**/*.java"
---

# Layered Architecture — Spring Boot 3.x

Use when generating or modifying any Spring Boot class — controllers, services, repositories,
DTOs, mappers, or configuration. Enforces strict layer separation and prevents business logic
from leaking across boundaries.

## Layer Rules

```
@RestController        ← HTTP only. No business logic. No JPA entities in responses.
      ↓ DTOs
@Service               ← All business logic lives here. Orchestrates repositories.
      ↓ Domain objects / Entities
@Repository            ← Data access only. No business logic. Returns entities or projections.
      ↓ JPA / JDBC
Database
```

## Controller Layer
- Handles HTTP: parsing requests, validating input (`@Valid`), returning responses
- Calls ONE service method per endpoint — no orchestration in controllers
- Never returns `@Entity` classes directly — always map to response DTOs
- Never injects `@Repository` — always goes through a `@Service`
- Exception handling via `@ControllerAdvice`, never try/catch in controllers

```java
// ✅ GOOD
@PostMapping("/orders")
public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    Order order = orderService.createOrder(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
}

// ❌ BAD — business logic in controller
@PostMapping("/orders")
public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
    if (request.getItems().isEmpty()) throw new RuntimeException("No items");
    Order order = orderRepository.save(new Order(request)); // direct repo access
    return ResponseEntity.ok(order); // returning entity
}
```

## Service Layer
- Contains all business logic, validation rules, and orchestration
- `@Transactional` lives here, not in controllers or repositories
- Constructor injection only — never `@Autowired` field injection
- One service per aggregate root (OrderService, not OrderAndPaymentService)
- Returns domain objects or DTOs — never `HttpServletRequest` / `HttpServletResponse`

```java
// ✅ GOOD
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        inventoryService.reserve(request.getItems());
        Order order = Order.from(request);
        return orderRepository.save(order);
    }
}
```

## Repository Layer
- Extends `JpaRepository<Entity, ID>` or `CrudRepository`
- Custom queries via `@Query` or query derivation — no raw SQL unless unavoidable
- Returns entities or Spring Data Projections — never raw `Object[]`
- No business logic — pure data access

## DTOs
- Separate Request / Response DTOs — never use the same class for both
- Validation annotations (`@NotNull`, `@Size`, etc.) on Request DTOs only
- Static factory method `ResponseDto.from(Entity entity)` for mapping
- Use records for immutable DTOs (Java 16+)

## Mapper Pattern
- Keep mapping logic out of controllers and services
- Entity → Response DTO: static method on the response DTO (`OrderResponse.from(order)`)
- Request DTO → Entity: static factory on the entity (`Order.from(request)`) or a mapper class
- Collection mapping: `.stream().map(OrderResponse::from).toList()`

## Configuration Layer
- `@Configuration` classes live in a `config/` package
- Use `@ConfigurationProperties` for type-safe config — never raw `@Value` for groups
- Bean definitions for infrastructure concerns only

## Cross-Cutting Concerns
- Logging: use `@Slf4j` — never `System.out.println`
- Validation: `@Valid` on controller parameters
- Exception handling: single `@RestControllerAdvice` class
- Auditing: `@CreatedDate` / `@LastModifiedDate` with `@EnableJpaAuditing`

## Gotchas
- Agent puts `@Transactional` on controllers — move to services
- Agent uses `@Autowired` field injection — use constructor injection (`@RequiredArgsConstructor`)
- Agent returns `List<Entity>` from controllers — map to `List<ResponseDto>`
- Agent creates god-class services — split by aggregate
- Agent puts mapping logic in controllers — extract to mapper or DTO factory
- Agent creates `@Configuration` that depends on `@Service` — config wires infrastructure only
