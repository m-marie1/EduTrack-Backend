package com.college.attendance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class to hold the database cleanup flag.
 * The actual cleanup logic is handled by ConditionalFlywayMigrationStrategy.
 */
@Configuration
@Profile("prod") // Only active in production profile
public class DatabaseCleanupConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCleanupConfig.class);

    @Value("${app.database.cleanup.enabled:false}")
    private boolean cleanupEnabled;

    // Constructor (optional, can be default)
    public DatabaseCleanupConfig() {
        logger.info("DatabaseCleanupConfig initialized. Cleanup enabled: {}", cleanupEnabled);
    }

    /**
     * Returns whether database cleanup is enabled via the application property.
     * @return true if cleanup is enabled, false otherwise.
     */
    public boolean isCleanupEnabled() {
        return cleanupEnabled;
    }
}