---
inclusion: fileMatch
fileMatchPattern: "**/security/**/*.java,**/auth/**/*.java,**/*Security*.java,**/*Jwt*.java"
---

# Spring Security — JWT Authentication

Use when implementing authentication, authorization, JWT tokens, security filters,
password encoding, or any Spring Security configuration.

## Dependencies

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

## Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, e) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("""
                        {"success":false,"error":{"code":"UNAUTHORIZED","message":"Authentication required"}}""");
                })
                .accessDeniedHandler((request, response, e) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("""
                        {"success":false,"error":{"code":"FORBIDDEN","message":"Insufficient permissions"}}""");
                })
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

## JWT Service

```java
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-token-expiry:900000}")
    private long accessTokenExpiry;

    @Value("${app.jwt.refresh-token-expiry:604800000}")
    private long refreshTokenExpiry;

    public String generateAccessToken(UserDetails user) {
        return generateToken(Map.of("type", "access"), user, accessTokenExpiry);
    }

    public String generateRefreshToken(UserDetails user) {
        return generateToken(Map.of("type", "refresh"), user, refreshTokenExpiry);
    }

    private String generateToken(Map<String, Object> claims, UserDetails user, long expiry) {
        return Jwts.builder()
            .claims(claims)
            .subject(user.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiry))
            .signWith(getSigningKey())
            .compact();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        return extractUsername(token).equals(user.getUsername()) && !isExpired(token);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
```

## JWT Filter

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            String username = jwtService.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails user = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, user)) {
                    var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException e) {
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(request, response);
    }
}
```

## application.yml

```yaml
app:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expiry: 900000
    refresh-token-expiry: 604800000
```

## Gotchas
- Agent uses old `csrf().disable()` API — use `AbstractHttpConfigurer::disable`
- Agent lets `ExpiredJwtException` escape the filter — becomes 500 instead of 401
- Agent skips `exceptionHandling()` — clients get empty 401/403 bodies
- Agent stores JWT secret in code — always `${JWT_SECRET}` from environment
- Agent uses `SessionCreationPolicy.IF_REQUIRED` — must be `STATELESS` for JWT
- Agent forgets `@EnableMethodSecurity` for `@PreAuthorize`
- Agent uses BCrypt strength < 10 — use 12 for production
- Agent puts refresh tokens in localStorage — recommend httpOnly cookies
