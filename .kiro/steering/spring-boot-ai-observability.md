---
inclusion: manual
---

# AI Observability — Spring Boot

Use when adding monitoring, metrics, logging, or tracing to Spring AI integrations.

## Spring AI Built-in Observability

```yaml
spring:
  ai:
    chat:
      observations:
        log-prompt: true       # OFF in prod (PII)
        log-completion: true
management:
  metrics:
    tags:
      application: order-service
  endpoints:
    web:
      exposure:
        include: health,prometheus,metrics
```

## Custom AI Metrics

```java
@Component
@RequiredArgsConstructor
public class AiMetrics {

    private final MeterRegistry meterRegistry;

    public <T> T track(String operation, String model, Supplier<T> call) {
        return Timer.builder("ai.prompt.latency")
            .tag("operation", operation)
            .tag("model", model)
            .register(meterRegistry)
            .recordCallable(() -> call.get());
    }

    public void recordTokens(String operation, String model, int inputTokens, int outputTokens) {
        Counter.builder("ai.tokens.used")
            .tag("operation", operation)
            .tag("model", model)
            .tag("type", "input")
            .register(meterRegistry)
            .increment(inputTokens);

        Counter.builder("ai.tokens.used")
            .tag("operation", operation)
            .tag("model", model)
            .tag("type", "output")
            .register(meterRegistry)
            .increment(outputTokens);
    }
}
```

## Audit Advisor (GA API)

```java
@Component
public class AiAuditAdvisor implements CallAdvisor {

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long start = System.currentTimeMillis();
        ChatClientResponse response = chain.nextCall(request);
        long latency = System.currentTimeMillis() - start;

        ChatResponse chatResponse = response.chatResponse();
        if (chatResponse != null && chatResponse.getMetadata() != null) {
            Usage usage = chatResponse.getMetadata().getUsage();
            log.info("[AI-AUDIT] latencyMs={} inputTokens={} outputTokens={}",
                latency, usage.getPromptTokens(), usage.getCompletionTokens());
        }
        return response;
    }

    @Override
    public String getName() { return "AiAuditAdvisor"; }
    @Override
    public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
}
```

## Cost Estimation

```java
@Service
public class AiCostEstimator {
    private static final Map<String, double[]> PRICING = Map.of(
        "claude-sonnet-4-20250514", new double[]{3.0, 15.0},
        "gpt-4o", new double[]{5.0, 15.0}
    );

    public double estimateCost(String model, int inputTokens, int outputTokens) {
        double[] prices = PRICING.getOrDefault(model, new double[]{5.0, 15.0});
        return (inputTokens * prices[0] + outputTokens * prices[1]) / 1_000_000;
    }
}
```

## Gotchas
- Agent implements `CallAroundAdvisor` — removed in GA; use `CallAdvisor`/`ChatClientRequest`
- Agent calls `usage.getGenerationTokens()` — GA renamed to `getCompletionTokens()`
- Agent logs full prompts in production — keep `log-prompt: false` for PII safety
- Agent skips async on audit saves — use `@Async` on a **separate bean**
- Agent hardcodes token pricing — extract to config
