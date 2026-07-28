---
inclusion: manual
---

# Domain-Driven Design — Spring Boot

Use when working with domain models, aggregates, value objects, domain events, or
repositories in a DDD-style project.

## Aggregate Rules
- One repository per aggregate root
- External code only accesses aggregate through root
- Aggregates reference other aggregates by ID only
- Keep aggregates small — max 3-4 child entities

## Value Objects

```java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Amount cannot be negative");
    }

    public Money add(Money other) {
        if (!currency.equals(other.currency))
            throw new CurrencyMismatchException(currency, other.currency);
        return new Money(amount.add(other.amount), currency);
    }
}

public record EmailAddress(String value) {
    public EmailAddress {
        if (!value.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$"))
            throw new InvalidEmailException(value);
    }
}
```

## Domain Events

```java
public record OrderPlaced(OrderId orderId, CustomerId customerId, Money total, Instant occurredAt) {
    public static OrderPlaced of(Order order) {
        return new OrderPlaced(order.getId(), order.getCustomerId(), order.getTotal(), Instant.now());
    }
}

// Collect events in aggregate, publish after save
@Entity
public class Order {
    @Transient
    private final List<Object> domainEvents = new ArrayList<>();

    public void place() {
        this.status = OrderStatus.PLACED;
        domainEvents.add(OrderPlaced.of(this));
    }

    public List<Object> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
```

## Event Listener — Bind to Commit

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Async
public void onOrderPlaced(OrderPlaced event) {
    emailService.sendOrderConfirmation(event.customerId(), event.orderId());
}
```

> Use `@DomainEvents` and `@AfterDomainEventPublication` on the aggregate root for automatic publishing via Spring Data.

## Specifications

```java
public class OrderSpecifications {
    public static Specification<Order> byStatus(OrderStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}

// Compose
orderRepository.findAll(
    OrderSpecifications.byStatus(PLACED)
        .and(OrderSpecifications.byCustomer(customerId)),
    pageable);
```

## Anti-Corruption Layer

When integrating with external systems, create a translation layer:

```java
@Component
@RequiredArgsConstructor
public class PaymentGatewayAdapter implements PaymentPort {
    private final ExternalPaymentClient client;

    @Override
    public PaymentConfirmation charge(OrderId orderId, Money amount) {
        PaymentApiRequest apiRequest = new PaymentApiRequest(
            orderId.value().toString(),
            amount.amount().doubleValue(),
            amount.currency().getCurrencyCode());

        PaymentApiResponse apiResponse = client.charge(apiRequest);

        return new PaymentConfirmation(
            PaymentId.of(apiResponse.getTransactionId()),
            apiResponse.isSuccessful() ? PaymentStatus.CONFIRMED : PaymentStatus.DECLINED);
    }
}
```

## Gotchas
- Agent creates anemic models with only getters/setters — put behavior on domain objects
- Agent uses `Long` for entity IDs — use typed value objects (`OrderId`, `CustomerId`)
- Agent puts domain logic in services — services should orchestrate, not decide
- Agent accesses child entities directly — always go through aggregate root
- Agent publishes events before saving — publish after successful save/commit
- Agent lets external API models into domain — use Anti-Corruption Layer
