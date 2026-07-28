---
inclusion: fileMatch
fileMatchPattern: "**/service/**/*.java,**/*Service.java"
---

# Transactional Patterns

Use when working with @Transactional, multi-step database operations, or any code that
needs atomicity guarantees.

## Basic Rules

- `@Transactional` belongs on **service methods**, never controllers or repositories
- Default propagation is `REQUIRED` — joins existing transaction or creates one
- `@Transactional(readOnly = true)` on all read-only service methods

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // default for all methods
public class OrderService {

    @Transactional // overrides readOnly for writes
    public Order createOrder(CreateOrderRequest request) {
        inventoryService.reserve(request.items());
        return orderRepository.save(Order.from(request));
    }

    public Optional<Order> findById(UUID id) {
        return orderRepository.findById(id); // readOnly inherited
    }
}
```

## Propagation

| Propagation | Behavior |
|-------------|----------|
| `REQUIRED` (default) | Join existing TX or create new |
| `REQUIRES_NEW` | Always create new TX, suspend existing |
| `SUPPORTS` | Join if exists, proceed without TX if not |
| `MANDATORY` | Must have existing TX, throw if not |

## Self-Invocation Pitfall

```java
// ❌ BROKEN — self-invocation bypasses Spring proxy
@Service
public class OrderService {
    @Transactional
    public void processAll(List<UUID> ids) {
        ids.forEach(id -> this.processSingle(id)); // bypasses proxy!
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingle(UUID id) { ... } // never creates new TX
}

// ✅ FIX — extract to separate bean
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderProcessor orderProcessor;

    @Transactional
    public void processAll(List<UUID> ids) {
        ids.forEach(id -> orderProcessor.processSingle(id));
    }
}
```

## Side Effects After Commit

Never fire external side effects inside the transaction:

```java
@Transactional
public Order place(UUID id) {
    Order order = orderRepository.findById(id).orElseThrow();
    order.place();
    eventPublisher.publishEvent(new OrderPlaced(order.getId()));
    return orderRepository.save(order);
}

@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onOrderPlaced(OrderPlaced event) {
    emailService.sendConfirmation(event.orderId()); // safe: data is durable
}
```

## Gotchas
- Agent puts `@Transactional` on controllers — only on service layer
- Agent sends email/publishes events inside TX — use `@TransactionalEventListener(AFTER_COMMIT)`
- Agent forgets `readOnly = true` on read methods — missed DB optimization
- Agent calls `@Transactional` methods on `this` — self-invocation bypasses proxy
- Agent expects checked exceptions to rollback — must add `rollbackFor`
- Agent uses `@Transactional` on `private` methods — Spring proxy can't intercept
