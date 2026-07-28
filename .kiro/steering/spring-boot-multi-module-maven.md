---
inclusion: fileMatch
fileMatchPattern: "**/pom.xml"
---

# Multi-Module Maven

Use when working in a multi-module Maven project.

## Typical Structure

```
my-app/
├── pom.xml                  ← Parent POM (packaging = pom)
├── my-app-domain/           ← Pure Java domain — no Spring
├── my-app-application/      ← Use cases — depends on domain
├── my-app-infrastructure/   ← JPA, Redis, HTTP clients
└── my-app-web/              ← Spring Boot app, REST — depends on all above
```

## Parent POM

```xml
<project>
    <groupId>com.example</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
    </parent>

    <modules>
        <module>my-app-domain</module>
        <module>my-app-application</module>
        <module>my-app-infrastructure</module>
        <module>my-app-web</module>
    </modules>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.example</groupId>
                <artifactId>my-app-domain</artifactId>
                <version>${project.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

## Dependency Rules

| Module | Can depend on | Cannot depend on |
|--------|---------------|------------------|
| `domain` | Nothing | Everything |
| `application` | `domain` | `infrastructure`, `web` |
| `infrastructure` | `domain`, `application` | `web` |
| `web` | All modules | — |

## Gotchas
- Agent puts `spring-boot-maven-plugin` in parent — only in runnable module
- Agent adds `<dependencies>` in parent instead of `<dependencyManagement>` — pollutes all modules
- Agent creates circular dependencies between modules
- Agent imports Spring in `domain` module — domain must be framework-free
