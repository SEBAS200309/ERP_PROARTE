package com.proarte.erp.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.AuditorAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

/**
 * Clase base abstracta para tests de integración con Testcontainers.
 * Provee un contenedor PostgreSQL compartido y configuración de auditoría para tests.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Import(BaseIntegrationTest.TestAuditingConfig.class)
public abstract class BaseIntegrationTest {

    static final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("erp_proarte_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    /**
     * Configuración de test que sobreescribe el AuditorAware para evitar
     * dependencia del contexto de seguridad en tests de integración.
     */
    @TestConfiguration
    static class TestAuditingConfig {
        @Bean
        AuditorAware<UUID> auditorAwareImpl() {
            return () -> Optional.of(TEST_USER_ID);
        }
    }
}
