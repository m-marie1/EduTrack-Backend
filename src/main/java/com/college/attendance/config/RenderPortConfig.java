package com.college.attendance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration to ensure Render.com can properly detect the port
 * This explicitly logs the port being used and ensures the application
 * binds to the correct port for environment detection.
 */
@Configuration
@Profile("prod")
public class RenderPortConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RenderPortConfig.class);
    
    /**
     * Customize the web server to use port 8080 explicitly for Render.com detection
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            // Always use port 8080 for Render.com regardless of the PORT env var
            // This ensures Render.com can detect the application properly
            logger.info("Configuring web server to use Render-compatible port: 8080");
            factory.setPort(8080);
        };
    }
} 