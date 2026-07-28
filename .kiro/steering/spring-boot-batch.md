---
inclusion: manual
---

# Spring Batch — Spring Boot 3.x (Batch 5)

Use when building batch jobs, ETL pipelines, scheduled imports/exports, or any chunk-oriented
bulk processing.

## Critical Rules for Spring Batch 5

1. **Do NOT add `@EnableBatchProcessing`** — Boot auto-configures everything. Adding it disables auto-config.
2. **`JobBuilderFactory` and `StepBuilderFactory` are gone** — use `new JobBuilder(name, jobRepository)`.

## Job & Step

```java
@Configuration
@RequiredArgsConstructor
public class OrderExportJobConfig {

    @Bean
    public Job orderExportJob(JobRepository jobRepository, Step exportStep) {
        return new JobBuilder("orderExportJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(exportStep)
            .build();
    }

    @Bean
    public Step exportStep(JobRepository jobRepository,
                           PlatformTransactionManager txManager,
                           ItemReader<Order> reader,
                           ItemProcessor<Order, OrderRow> processor,
                           ItemWriter<OrderRow> writer) {
        return new StepBuilder("exportStep", jobRepository)
            .<Order, OrderRow>chunk(500, txManager)
            .reader(reader)
            .processor(processor)
            .writer(writer)
            .faultTolerant()
            .skip(FlatFileParseException.class)
            .skipLimit(50)
            .build();
    }
}
```

## ItemReader — Sort Key Required

```java
@Bean
@StepScope
public JpaPagingItemReader<Order> orderReader(
        EntityManagerFactory emf,
        @Value("#{jobParameters['status']}") String status) {
    return new JpaPagingItemReaderBuilder<Order>()
        .name("orderReader")
        .entityManagerFactory(emf)
        .queryString("SELECT o FROM Order o WHERE o.status = :status ORDER BY o.id")
        .parameterValues(Map.of("status", OrderStatus.valueOf(status)))
        .pageSize(500)
        .build();
}
```

## ItemWriter — Batch 5 Signature

```java
// Batch 5: Chunk<? extends T>, not List<? extends T>
@Override
public void write(Chunk<? extends OrderRow> chunk) {
    repository.saveAll(chunk.getItems());
}
```

## Launching Jobs

```yaml
spring:
  batch:
    job:
      enabled: false           # don't run on startup
    jdbc:
      initialize-schema: never # use Flyway in prod
```

```java
@Scheduled(cron = "0 0 2 * * *")
public void runNightly() throws JobExecutionException {
    JobParameters params = new JobParametersBuilder()
        .addString("status", "COMPLETED")
        .addLong("run.id", System.currentTimeMillis())
        .toJobParameters();
    jobLauncher.run(orderExportJob, params);
}
```

## Gotchas
- Agent adds `@EnableBatchProcessing` — **disables** auto-config on Boot 3
- Agent uses `JobBuilderFactory` / `StepBuilderFactory` — removed in Batch 5
- Agent calls `.chunk(500)` without txManager — Batch 5 requires `.chunk(500, txManager)`
- Agent writes `write(List<...>)` — Batch 5 is `write(Chunk<...>)`
- Agent writes paging reader without `ORDER BY` — pages skip/duplicate rows silently
- Agent uses `JdbcCursorItemReader` in multi-threaded step — not thread-safe
- Agent returns `null` from processor expecting pass-through — `null` filters (drops) the item
- Agent sends email from `ItemWriter` — runs inside chunk TX; use `afterJob` listener
- Agent forgets `@StepScope` on reader with `jobParameters` — binding fails
- Agent leaves jobs running on startup — set `spring.batch.job.enabled=false`
