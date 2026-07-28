---
inclusion: fileMatch
fileMatchPattern: "**/db/migration/**/*.sql,**/flyway/**"
---

# Flyway Migrations

Use when creating database migrations, schema changes, seed data, or any SQL that
modifies database structure.

## File Naming Convention

```
src/main/resources/db/migration/

V{version}__{description}.sql       ← versioned (run once)
R__{description}.sql                ← repeatable (run when checksum changes)

Examples:
V1__create_users_table.sql
V2__create_orders_table.sql
V2.1__add_order_status_index.sql
```

Rules:
- Double underscore `__` between version and description
- Underscore `_` for spaces in description
- Sequential versions — never go back and fill gaps
- Never modify a migration that has already run in any environment

## Safe Migration Patterns

```sql
-- ✅ Safe: add nullable column
ALTER TABLE orders ADD COLUMN notes TEXT;

-- ✅ Safe: add column with default
ALTER TABLE orders ADD COLUMN priority INT NOT NULL DEFAULT 0;

-- ✅ Safe: add index CONCURRENTLY (needs executeInTransaction=false)
CREATE INDEX CONCURRENTLY idx_orders_email ON orders(customer_email);

-- ❌ Dangerous: rename column directly
ALTER TABLE orders RENAME COLUMN user_id TO customer_id;

-- ❌ Dangerous: NOT NULL without default on large table
ALTER TABLE orders ADD COLUMN priority INT NOT NULL;
```

## application.yml

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
    out-of-order: false
```

## Seed Data (dev only)

Use Spring profiles, not Flyway, for seed data:

```java
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() == 0) {
            userRepository.save(User.createAdmin("admin@dev.local", "password123"));
        }
    }
}
```

## Gotchas
- Agent names files with single underscore — must be double `__`
- Agent modifies existing migration files — never edit a migration that has run
- Agent adds `NOT NULL` column without default — use nullable or provide default
- Agent renames columns directly — use multi-step add/backfill/drop
- Agent seeds data in Flyway migrations — use `@Profile("dev")` seeders
- Agent uses `CREATE INDEX CONCURRENTLY` in normal migration — needs `executeInTransaction=false`
- Agent skips indexes — always index foreign keys and WHERE/ORDER BY columns
