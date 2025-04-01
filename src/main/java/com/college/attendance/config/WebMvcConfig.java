package com.college.attendance.config;

import com.college.attendance.interceptor.MetricsInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for Spring MVC
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final MetricsInterceptor metricsInterceptor;

    public WebMvcConfig(MetricsInterceptor metricsInterceptor) {
        this.metricsInterceptor = metricsInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the metrics interceptor for all API endpoints
        registry.addInterceptor(metricsInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health", "/api/info");
    }
} 