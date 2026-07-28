---
inclusion: manual
---

# OAuth2 Resource Server

Use when configuring Spring Boot as an OAuth2 resource server, validating JWTs from
an external auth provider (Keycloak, Auth0, Okta, Cognito).

## Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

## Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/admin/**").hasAuthority("SCOPE_admin")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
            )
            .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        var converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("roles");
        converter.setAuthorityPrefix("ROLE_");
        var authConverter = new JwtAuthenticationConverter();
        authConverter.setJwtGrantedAuthoritiesConverter(converter);
        return authConverter;
    }
}
```

## application.yml

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://keycloak.example.com/realms/my-realm
```

## Controller — Accessing Current User

```java
@GetMapping("/api/v1/orders/my")
public ApiResponse<List<OrderResponse>> myOrders(@AuthenticationPrincipal Jwt jwt) {
    UUID userId = UUID.fromString(jwt.getSubject());
    return ApiResponse.ok(orderService.findByUser(userId));
}
```

## Method Security

```java
@PreAuthorize("hasAuthority('SCOPE_orders:read')")
public List<Order> findAll() { ... }

@PreAuthorize("hasRole('ADMIN') or @orderSecurity.isOwner(#orderId, authentication)")
public Order findById(UUID orderId) { ... }
```

## Gotchas
- Agent uses `hasRole("ADMIN")` for scopes — use `hasAuthority("SCOPE_admin")`
- Agent forgets `issuer-uri` validation — always configure
- Agent maps Keycloak roles wrong — they're nested under `realm_access.roles`
- Agent adds `userDetailsService` bean — not needed for resource servers
