---
inclusion: fileMatch
fileMatchPattern: "**/entity/**/*.java,**/repository/**/*.java,**/model/**/*.java,**/*Entity.java,**/*Repository.java"
---

# Spring Data JPA — Conventions

Use when generating JPA entities, repositories, queries, or anything touching the persistence
layer. Covers entity conventions, N+1 prevention, projections, and query patterns.

## Entity Conventions

```java
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String customerEmail;

    @Enumerated(EnumType.STRING) // always STRING, never ORDINAL
    @Column(nullable = false)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public static Order create(String customerEmail) {
        Order order = new Order();
        order.customerEmail = customerEmail;
        order.status = OrderStatus.PENDING;
        return order;
    }

    public void addItem(Product product, int quantity) {
        items.add(OrderItem.create(this, product, quantity));
    }
}
```

## Rules
- `@Enumerated(EnumType.STRING)` always — `ORDINAL` breaks on enum reordering
- `GenerationType.UUID` for IDs — never expose auto-increment integers
- `@NoArgsConstructor(access = PROTECTED)` — required by JPA, hidden from app code
- `@Getter` from Lombok — no `@Setter` on entities (use behavior methods)
- Collections initialized inline (`= new ArrayList<>()`) — never null

## N+1 Prevention

**Fix with JOIN FETCH:**
```java
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
Optional<Order> findByIdWithItems(@Param("id") UUID id);

@EntityGraph(attributePaths = {"items", "items.product"})
List<Order> findByStatus(OrderStatus status);
```

**Fix with Projections for read-only views:**
```java
public interface OrderSummary {
    UUID getId();
    String getCustomerEmail();
    OrderStatus getStatus();
    Instant getCreatedAt();
}
List<OrderSummary> findByStatus(OrderStatus status);
```

## Bidirectional Relationships

```java
// Parent side
@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
private List<OrderItem> items = new ArrayList<>();

// Child side — owns the FK
@ManyToOne(fetch = FetchType.LAZY) // LAZY always on @ManyToOne
@JoinColumn(name = "order_id", nullable = false)
private Order order;
```

## Deep Pagination — Keyset over OFFSET

For large datasets, paginate by the last seen key:

```java
@Query("""
    SELECT o FROM Order o
    WHERE o.status = :status
      AND (o.createdAt < :lastCreatedAt
           OR (o.createdAt = :lastCreatedAt AND o.id < :lastId))
    ORDER BY o.createdAt DESC, o.id DESC
    """)
List<Order> findNextPage(OrderStatus status, Instant lastCreatedAt, UUID lastId, Limit limit);
```

## Batch Inserts

```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc.batch_size: 50
        order_inserts: true
        order_updates: true
```

## Gotchas
- Agent uses `FetchType.EAGER` — always use `LAZY` on `@ManyToOne` and `@ManyToMany`
- Agent uses `@Enumerated(EnumType.ORDINAL)` — always use `STRING`
- Agent uses `Long` IDs — use `UUID`
- Agent calls `findAll()` for list endpoints — always use `Pageable`
- Agent adds setters to entities — use behavior methods instead
- Agent forgets `orphanRemoval = true` on `@OneToMany`
- Agent writes N+1 without realizing — check for collection access in loops
- Agent batches inserts with `GenerationType.IDENTITY` — batching is silently off; use UUID
