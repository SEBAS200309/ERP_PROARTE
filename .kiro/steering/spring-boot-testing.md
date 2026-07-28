---
inclusion: fileMatch
fileMatchPattern: "**/test/**/*.java,**/*Test.java,**/*Tests.java,**/*IT.java"
---

# Testing Pyramid — Spring Boot 3.x

Use when writing tests of any kind — unit, slice, or integration.

## Structure

```
Unit Tests         — fast, no Spring context, mock dependencies    (70%)
Slice Tests        — partial Spring context (@WebMvcTest, @DataJpaTest) (20%)
Integration Tests  — full context + real DB via Testcontainers     (10%)
```

## Unit Tests — Services

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private InventoryService inventoryService;
    @InjectMocks private OrderService orderService;

    @Test
    void createOrder_whenItemsAvailable_shouldSaveAndReturnOrder() {
        // Given
        var request = new CreateOrderRequest("user@example.com", List.of(...));
        when(orderRepository.save(any(Order.class))).thenReturn(Order.create("user@example.com"));

        // When
        Order result = orderService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }
}
```

## Slice Tests — Controllers (@WebMvcTest)

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean OrderService orderService; // Spring Boot 3.4+: @MockBean is deprecated

    @Test
    @WithMockUser(roles = "USER")
    void createOrder_withValidRequest_shouldReturn201() throws Exception {
        when(orderService.createOrder(any())).thenReturn(Order.create("user@example.com"));

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

## Slice Tests — Repositories (@DataJpaTest)

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(TestcontainersConfig.class)
class OrderRepositoryTest {

    @Autowired OrderRepository orderRepository;

    @Test
    void findByStatus_shouldReturnMatchingOrders() {
        var order = orderRepository.save(Order.create("a@example.com"));
        List<Order> pending = orderRepository.findByStatus(OrderStatus.PENDING, Pageable.unpaged()).getContent();
        assertThat(pending).hasSize(1);
    }
}
```

## Integration Tests — Testcontainers

```java
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:16-alpine");
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
class OrderIntegrationTest {
    @Autowired TestRestTemplate restTemplate;
    // ... end-to-end tests
}
```

## Naming Convention

```
methodName_condition_expectedBehavior()
createOrder_whenItemsAvailable_shouldSaveOrder()
findById_whenOrderNotFound_shouldThrowNotFoundException()
```

## Gotchas
- Agent uses `@SpringBootTest` for everything — use slices for speed
- Agent uses H2 in-memory DB — use Testcontainers for accuracy
- Agent uses `@MockBean` — deprecated since 3.4; use `@MockitoBean`
- Agent uses `assertEquals` from JUnit — use AssertJ (`assertThat(...)`)
- Agent forgets `@WithMockUser` on controller tests — security blocks all requests
- Agent names tests `test_createOrder()` — use `createOrder_condition_expected()` pattern
