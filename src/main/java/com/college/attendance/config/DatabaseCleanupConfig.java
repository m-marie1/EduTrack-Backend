package com.college.attendance.config;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuration to perform a one-time cleanup of the database before migrations
 * This will run only in production and is intended for the initial deployment
 */
@Configuration
@Profile("prod")
public class DatabaseCleanupConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseCleanupConfig.class);

    @Value("${app.database.cleanup.enabled:false}")
    private boolean cleanupEnabled;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DatabaseCleanupConfig(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Perform a one-time cleanup of the database schema.
     * This is only intended for initial deployment and should be disabled afterward.
     */
    @PostConstruct
    public void cleanup() {
        if (!cleanupEnabled) {
            logger.info("Database cleanup is disabled. Skipping...");
            return;
        }

        logger.warn("INITIATING DATABASE CLEANUP - ALL DATA WILL BE LOST!");
        try {
            // Check if flyway_schema_history exists
            boolean tableExists = false;
            try {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'flyway_schema_history'", 
                    Integer.class
                );
                tableExists = (count != null && count > 0);
            } catch (Exception e) {
                logger.info("Flyway history table doesn't exist yet. This appears to be a fresh install.");
            }

            if (tableExists) {
                logger.info("Cleaning database schema with Flyway...");
                Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .cleanDisabled(false)
                    .load();
                
                flyway.clean();
                logger.info("Database schema cleaned successfully.");
            } else {
                logger.info("No Flyway history found. No cleanup needed.");
            }
        } catch (Exception e) {
            logger.error("Error during database cleanup", e);
            // Continue with application startup even if cleanup fails
        }
    }
} 