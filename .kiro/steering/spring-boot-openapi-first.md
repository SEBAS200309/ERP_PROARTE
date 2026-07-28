---
inclusion: manual
---

# OpenAPI-First Development

Use when the project follows API-first approach: generating controller interfaces and DTOs
from an OpenAPI spec. Use when you see openapi.yaml or openapi-generator-maven-plugin.

## Maven Plugin Setup

```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>7.5.0</version>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
            <configuration>
                <inputSpec>${project.basedir}/src/main/resources/openapi.yaml</inputSpec>
                <generatorName>spring</generatorName>
                <apiPackage>com.example.api</apiPackage>
                <modelPackage>com.example.api.model</modelPackage>
                <configOptions>
                    <delegatePattern>true</delegatePattern>
                    <useSpringBoot3>true</useSpringBoot3>
                    <useTags>true</useTags>
                    <openApiNullable>false</openApiNullable>
                    <skipDefaultInterface>true</skipDefaultInterface>
                </configOptions>
                <output>${project.build.directory}/generated-sources/openapi</output>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Implementing the Delegate

```java
@Service
@RequiredArgsConstructor
public class OrdersApiDelegateImpl implements OrdersApiDelegate {

    private final OrderService orderService;

    @Override
    public ResponseEntity<OrderResponse> createOrder(CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(OrderApiMapper.toResponse(order));
    }
}
```

## .gitignore

```gitignore
target/generated-sources/openapi/
```

## Gotchas
- Agent modifies generated controller files — NEVER modify generated code, implement delegate
- Agent generates without `useSpringBoot3=true` — uses old `javax.*` imports
- Agent commits generated sources — add to `.gitignore`
- Agent skips `skipDefaultInterface=true` — hides missing implementations
- Agent mixes generated and hand-written models — keep them separate
