---
inclusion: manual
---

# MCP Server — Java SDK (Spring Boot 3.x)

Use when building MCP (Model Context Protocol) servers in Java/Spring Boot. Covers tool
registration, resource exposure, and production deployment using the official MCP Java SDK.

Official Java SDK: https://github.com/modelcontextprotocol/java-sdk

## Dependencies

Most Spring Boot apps should use the **Spring AI MCP starter**:

```xml
<!-- stdio (local launching) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server</artifactId>
</dependency>
<!-- OR remote HTTP (SSE + Streamable-HTTP) -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

> The old `spring-ai-mcp-server-spring-boot-starter` name is dead. GA is `spring-ai-starter-mcp-server`.

## Spring Boot Integration (recommended)

```java
@Configuration
public class McpToolsConfig {

    @Bean
    public ToolCallbackProvider orderTools(OrderService orderService, ObjectMapper objectMapper) {
        return MethodToolCallbackProvider.builder()
            .toolObjects(new OrderMcpTools(orderService, objectMapper))
            .build();
    }
}

@Component
public class OrderMcpTools {
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Tool(description = "Get order by ID with full line items and status history")
    public String getOrder(@ToolParam(description = "UUID of the order") String orderId) {
        try {
            Order order = orderService.findById(UUID.fromString(orderId));
            return objectMapper.writeValueAsString(OrderResponse.from(order));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
```

## application.yml for MCP Server

```yaml
spring:
  ai:
    mcp:
      server:
        name: order-service-mcp
        version: 1.0.0
        type: SYNC
        stdio: true
  main:
    banner-mode: off  # stdio servers must keep stdout clean
```

## Error Handling Pattern

```java
private CallToolResult safeExecute(Supplier<Object> action) {
    try {
        return new CallToolResult(
            List.of(new TextContent(objectMapper.writeValueAsString(action.get()))),
            false
        );
    } catch (EntityNotFoundException e) {
        return errorResult("NOT_FOUND", e.getMessage());
    } catch (Exception e) {
        log.error("Tool execution failed", e);
        return errorResult("INTERNAL_ERROR", "Unexpected error occurred");
    }
}
```

## Gotchas
- Agent generates Python MCP code — always use the Java SDK
- Agent uses dead `spring-ai-mcp-server-spring-boot-starter` name — GA is `spring-ai-starter-mcp-server`
- Agent logs to stdout on a stdio server — corrupts JSON-RPC framing; banner off, logs to file/stderr
- Agent forgets `isError = true` in error results — agent can't distinguish errors from data
- Agent writes vague tool descriptions — be specific about when to call and what it returns
- `stdio` for local tools; `-webmvc`/`-webflux` + Streamable-HTTP for remote
