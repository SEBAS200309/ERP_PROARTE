---
inclusion: fileMatch
fileMatchPattern: "**/domain/**/*.java,**/port/**/*.java,**/adapter/**/*.java,**/infrastructure/**/*.java"
---

# Hexagonal Architecture (Ports & Adapters)

Use when the project follows hexagonal architecture. Prevents domain code from depending
on Spring or JPA. Use when you see packages like domain/, application/, infrastructure/.

## Package Structure

```
src/main/java/com/example/
├── domain/                     ← Pure Java. Zero framework dependencies.
│   ├── model/                  ← Entities, value objects, aggregates
│   ├── port/
│   │   ├── in/                 ← Use case interfaces (driving ports)
│   │   └── out/                ← Repository/external interfaces (driven ports)
│   └── service/                ← Domain services (pure business logic)
├── application/                ← Orchestrates use cases. Spring allowed here.
│   └── usecase/                ← @Service implementations of domain ports
└── infrastructure/             ← All framework/DB/HTTP details
    ├── persistence/            ← JPA adapters implementing out ports
    ├── web/                    ← REST controllers (driving adapters)
    └── external/               ← HTTP clients, messaging adapters
```

## Domain Layer — Zero Spring

```java
// Pure Java, no annotations
public class Order {
    private final OrderId id;
    private final CustomerId customerId;
    private OrderStatus status;

    public static Order create(CustomerId customerId) {
        return new Order(OrderId.generate(), customerId);
    }

    public void addItem(ProductId productId, int quantity, Money price) {
        if (status != OrderStatus.PENDING)
            throw new OrderNotModifiableException(id);
        items.add(new OrderItem(productId, quantity, price));
    }
}

// Value object
public record OrderId(UUID value) {
    public static OrderId generate() { return new OrderId(UUID.randomUUID()); }
}
```

## Ports — Interfaces Only

```java
// Driving port (what app offers)
public interface CreateOrderUseCase {
    Order createOrder(CreateOrderCommand command);
}

// Driven port (what app needs)
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
}
```

## Application Layer — Use Case Implementation

```java
@Service
@RequiredArgsConstructor
@Transactional
public class CreateOrderService implements CreateOrderUseCase {
    private final OrderRepository orderRepository;   // domain port
    private final InventoryPort inventoryPort;        // domain port

    @Override
    public Order createOrder(CreateOrderCommand command) {
        Order order = Order.create(command.customerId());
        inventoryPort.reserve(order.getItems());
        return orderRepository.save(order);
    }
}
```

## Infrastructure — Adapters

```java
// Persistence adapter
@Repository
@RequiredArgsConstructor
public class JpaOrderRepository implements OrderRepository {
    private final SpringDataOrderRepository springDataRepo;
    private final OrderMapper mapper;

    @Override
    public Order save(Order order) {
        return mapper.toDomain(springDataRepo.save(mapper.toEntity(order)));
    }
}

// Web adapter
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
        Order order = createOrderUseCase.createOrder(request.toCommand());
        return ResponseEntity.status(201).body(ApiResponse.ok(OrderResponse.from(order)));
    }
}
```

## Gotchas
- Agent imports `javax.persistence` in domain — domain must be framework-free
- Agent injects `JpaRepository` directly into use cases — use domain port interfaces
- Agent puts `@Transactional` on domain services — belongs in application layer
- Agent mixes driving and driven ports — `port/in` = what app offers, `port/out` = what app needs
- Agent creates anemic domain with only getters/setters — behavior belongs on domain objects
