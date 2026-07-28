---
inclusion: manual
---

# Spring AI Integration

Use when integrating LLMs, chat clients, embeddings, RAG pipelines, or AI agents into
Spring Boot. Covers Spring AI ChatClient, prompt templates, structured output.

## Dependencies

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <!-- GA renamed every starter to spring-ai-starter-* -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-anthropic</artifactId>
    </dependency>
    <!-- For RAG -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-vector-store-pgvector</artifactId>
    </dependency>
</dependencies>
```

## ChatClient — Basic Usage

```java
@Service
@RequiredArgsConstructor
public class DocumentSummaryService {

    private final ChatClient chatClient;

    public String summarize(String content) {
        return chatClient.prompt()
            .user(u -> u.text("Summarize:\n\n{content}")
                .param("content", content))
            .call()
            .content();
    }
}
```

## ChatClient Bean Configuration

```java
@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
            .maxMessages(20)
            .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
            .defaultSystem("You are a helpful assistant.")
            .defaultAdvisors(
                MessageChatMemoryAdvisor.builder(chatMemory).build(),
                new SimpleLoggerAdvisor()
            )
            .build();
    }
}
```

## Structured Output

```java
public record OrderClassification(String category, String priority, List<String> tags) {}

public OrderClassification classify(String orderDescription) {
    return chatClient.prompt()
        .user("Classify this order: " + orderDescription)
        .call()
        .entity(OrderClassification.class);
}
```

## RAG Pipeline

```java
@Bean
public ChatClient ragChatClient(ChatClient.Builder builder, VectorStore vectorStore) {
    return builder
        .defaultAdvisors(
            QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(5).build())
                .build()
        )
        .build();
}
```

## application.yml

```yaml
spring:
  ai:
    anthropic:
      api-key: ${ANTHROPIC_API_KEY}
      chat:
        options:
          model: claude-sonnet-4-20250514
          max-tokens: 2048
          temperature: 0.7
    vectorstore:
      pgvector:
        initialize-schema: true
        dimensions: 1536
```

## Gotchas
- Agent uses pre-GA artifact names — GA is `spring-ai-starter-model-<provider>`
- Agent writes `new InMemoryChatMemory()` — removed in GA; use `MessageWindowChatMemory`
- Agent writes `SearchRequest.defaults().withTopK(n)` — GA is `SearchRequest.builder().topK(n).build()`
- Agent hardcodes API keys — always use environment variables
- Agent builds prompts with string concatenation — use `.param()` template variables
- Agent skips error handling — handle `NonTransientAiException` vs `TransientAiException`
- Agent forgets per-user `conversationId` — all users share one chat history
