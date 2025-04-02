package com.college.attendance.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * Configuration to ensure Render.com can properly detect the port
 * This explicitly logs the port being used and ensures the application
 * binds to the correct port for environment detection.
 */
@Configuration
@Profile("prod")
public class RenderPortConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RenderPortConfig.class);
    
    @Value("${PORT:8080}")
    private int port;
    
    /**
     * Customize the web server to use the PORT environment variable
     * and log the port being used to help with Render.com detection.
     */
    @Bean
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {
            logger.info("Configuring web server to use port: {}", port);
            factory.setPort(port);
        };
    }
} 