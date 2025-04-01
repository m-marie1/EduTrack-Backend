package com.college.attendance.interceptor;

import com.college.attendance.service.MetricsService;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Interceptor to track API metrics
 */
@Component
public class MetricsInterceptor implements HandlerInterceptor {

    private final MetricsService metricsService;
    private final ConcurrentHashMap<String, Timer> endpointTimers = new ConcurrentHashMap<>();
    private static final String START_TIME_ATTRIBUTE = "startTime";

    public MetricsInterceptor(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Increment API request counter
        metricsService.incrementApiRequest();
        
        // Store start time for timing the request
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // This method is not used
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Record request duration
        Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Record detailed metrics only for API endpoints
            if (handler instanceof HandlerMethod) {
                recordEndpointMetrics((HandlerMethod) handler, request, duration, response.getStatus());
            }
        }
    }

    private void recordEndpointMetrics(HandlerMethod handlerMethod, HttpServletRequest request, long duration, int statusCode) {
        // Create a metric name based on the controller and method
        String className = handlerMethod.getBeanType().getSimpleName();
        String methodName = handlerMethod.getMethod().getName();
        String endpoint = String.format("%s.%s", className, methodName);
        
        // Get or create a timer for this endpoint
        Timer timer = endpointTimers.computeIfAbsent(endpoint, name -> 
            metricsService.timer(
                "app.endpoint.duration",
                "endpoint", endpoint,
                "method", request.getMethod()
            )
        );
        
        // Record the duration
        timer.record(duration, TimeUnit.MILLISECONDS);
        
        // Track status code metrics separately
        String statusCategory = statusCode >= 500 ? "5xx" : 
                               statusCode >= 400 ? "4xx" : 
                               statusCode >= 300 ? "3xx" : 
                               statusCode >= 200 ? "2xx" : "other";
        
        metricsService.counter(
            "app.http.status", 
            "endpoint", endpoint,
            "method", request.getMethod(),
            "status", String.valueOf(statusCode),
            "category", statusCategory
        ).increment();
    }
} 