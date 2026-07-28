---
inclusion: manual
---

# Spring HATEOAS

Use when adding hypermedia links to REST responses or implementing Spring HATEOAS.

## When to Add Links
- `self` — always, on every resource response
- `collection` — link back to the list endpoint
- `related resources` — when client commonly needs navigation
- `actions` — links to state transitions when valid for current state

## RepresentationModelAssembler (recommended)

```java
@Component
public class OrderModelAssembler implements RepresentationModelAssembler<Order, EntityModel<OrderResponse>> {

    @Override
    public EntityModel<OrderResponse> toModel(Order order) {
        EntityModel<OrderResponse> model = EntityModel.of(OrderResponse.from(order),
            linkTo(methodOn(OrderController.class).getById(order.getId())).withSelfRel(),
            linkTo(methodOn(OrderController.class).list(null)).withRel("orders"));

        if (order.getStatus() == OrderStatus.PENDING) {
            model.add(linkTo(methodOn(OrderController.class)
                .cancelOrder(order.getId())).withRel("cancel"));
        }
        return model;
    }
}
```

## PagedModel for Pagination

```java
@GetMapping
public ResponseEntity<PagedModel<EntityModel<OrderResponse>>> list(
        Pageable pageable, PagedResourcesAssembler<Order> pagedAssembler) {
    Page<Order> orders = orderService.findAll(pageable);
    return ResponseEntity.ok(pagedAssembler.toModel(orders, orderModelAssembler));
}
```

## Gotchas
- Agent adds all links regardless of state — only add action links when valid
- Agent hardcodes URLs — use `linkTo(methodOn(...))` for type-safe links
- Agent returns plain DTO — wrap in `EntityModel.of(dto, links...)`
- Agent puts link logic in controller — extract to `RepresentationModelAssembler`
- Agent manually builds pagination links — use `PagedResourcesAssembler`
- Agent forgets `self` link — every resource must have one
