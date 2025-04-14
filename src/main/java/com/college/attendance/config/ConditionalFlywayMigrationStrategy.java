package com.college.attendance.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Custom Flyway migration strategy that conditionally cleans the database
 * before migrating, based on the 'app.database.cleanup.enabled' property.
 * This strategy is only active in the 'prod' profile.
 */
@Configuration
@Profile("prod") // Only active in production profile
public class ConditionalFlywayMigrationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ConditionalFlywayMigrationStrategy.class);

    @Autowired
    private DatabaseCleanupConfig databaseCleanupConfig; // Inject the config holder

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            if (databaseCleanupConfig.isCleanupEnabled()) {
                logger.warn("INITIATING DATABASE CLEANUP via FlywayMigrationStrategy - ALL DATA WILL BE LOST!");
                try {
                    // *** FIX: Create a new Flyway instance based on the existing configuration ***
                    // *** but explicitly allow cleaning for this operation. ***
                    Flyway cleanFlyway = Flyway.configure()
                            .configuration(flyway.getConfiguration()) // Copy existing config
                            .cleanDisabled(false) // *** Explicitly allow clean ***
                            .load();

                    cleanFlyway.clean(); // *** Use the clean-enabled instance ***

                    logger.info("Database schema cleaned successfully via FlywayMigrationStrategy.");

                    // After cleaning, proceed with migration
                    // After cleaning, proceed with migration using the original flyway instance
                    logger.info("Proceeding with Flyway migration after cleanup...");
                    flyway.migrate(); // Use the original instance for migration
                    logger.info("Flyway migration completed successfully after cleanup.");

                } catch (Exception e) {
                    logger.error("Error during database cleanup or migration within FlywayMigrationStrategy", e);
                    // It's generally safer to halt if cleanup fails unexpectedly
                    throw new RuntimeException("Failed during conditional cleanup/migration", e);
                    // logger.warn("Attempting migration even after cleanup error...");
                    // flyway.migrate(); // Attempt migration anyway - removed for safety
                }
            } else {
                logger.info("Database cleanup is disabled. Proceeding with standard Flyway migration...");
                flyway.migrate(); // Standard migration without cleaning
                logger.info("Standard Flyway migration completed successfully.");
            }
        };
    }
}