---
inclusion: manual
---

# Spring Data Redis — Caching

Use when implementing caching, session storage, rate limiting, or any Redis integration.

## Configuration

```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .withCacheConfiguration("orders", config.entryTtl(Duration.ofMinutes(5)))
            .withCacheConfiguration("products", config.entryTtl(Duration.ofHours(1)))
            .build();
    }
}
```

## Key Naming Convention

```
{app}:{domain}:{id}          → orders:order:uuid-here
{app}:{domain}:list:{filter} → orders:order:list:status:PENDING
{app}:ratelimit:{ip}         → orders:ratelimit:192.168.1.1
```

## @Cacheable — Declarative Caching

```java
@Cacheable(value = "products", key = "#id", sync = true)
public ProductResponse findById(UUID id) { ... }

@CachePut(value = "products", key = "#result.id")
@Transactional
public ProductResponse update(UUID id, UpdateProductRequest request) { ... }

@CacheEvict(value = "products", key = "#id")
@Transactional
public void delete(UUID id) { ... }
```

## Rate Limiting

```java
@Component
@RequiredArgsConstructor
public class RateLimiter {
    private final RedisTemplate<String, String> redisTemplate;

    public boolean isAllowed(String identifier, int maxRequests, Duration window) {
        String key = "ratelimit:" + identifier;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, window);
        }
        return count <= maxRequests;
    }
}
```

## application.yml

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
  cache:
    type: redis
```

## Gotchas
- Agent uses Java serialization — always use JSON (`GenericJackson2JsonRedisSerializer`)
- Agent caches entities with JPA lazy fields — cache DTOs, not entities
- Agent uses no TTL — always set expiry
- Agent forgets `@EnableCaching` — `@Cacheable` silently does nothing
- Agent caches `null` values — use `.disableCachingNullValues()`
- Agent leaves hot keys unprotected — use `sync = true` to prevent stampede
