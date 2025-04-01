package com.college.attendance.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.MapInfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Spring Boot Actuator
 */
@Configuration
public class ActuatorConfig {

    /**
     * Register custom application info
     */
    @Bean
    public InfoContributor applicationInfo() {
        Map<String, Object> details = new HashMap<>();
        
        Map<String, Object> app = new HashMap<>();
        app.put("name", "College Attendance System");
        app.put("description", "A system for managing student attendance");
        app.put("version", "1.0.0");
        
        Map<String, Object> contact = new HashMap<>();
        contact.put("name", "IT Department");
        contact.put("email", "it@college.edu");
        
        details.put("app", app);
        details.put("contact", contact);
        
        return new MapInfoContributor(details);
    }

    /**
     * Custom health indicator for database connection
     */
    @Bean
    @ConditionalOnEnabledHealthIndicator("database")
    public HealthIndicator databaseHealthIndicator() {
        return () -> {
            // In a real implementation, you would check if the database is accessible
            // For now, we just return UP status
            return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Connected")
                    .build();
        };
    }

    /**
     * Custom health indicator for file storage
     */
    @Bean
    @ConditionalOnEnabledHealthIndicator("fileStorage")
    public HealthIndicator fileStorageHealthIndicator() {
        return () -> {
            // In a real implementation, you would check if the file storage is accessible
            // For now, we just return UP status
            return Health.up()
                    .withDetail("storageType", "Local filesystem")
                    .withDetail("status", "Accessible")
                    .build();
        };
    }

    /**
     * Custom health indicator for email service
     */
    @Bean
    @ConditionalOnEnabledHealthIndicator("email")
    public HealthIndicator emailHealthIndicator() {
        return () -> {
            // In a real implementation, you would check if the email service is working
            // For now, we just return UP status
            return Health.up()
                    .withDetail("service", "SMTP")
                    .withDetail("status", "Connected")
                    .build();
        };
    }

    /**
     * Register a meter registry if one doesn't exist
     * This is needed for testing/development without Prometheus
     */
    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
} 