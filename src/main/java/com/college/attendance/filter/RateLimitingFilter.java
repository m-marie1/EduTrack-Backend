package com.college.attendance.filter;

import com.college.attendance.controller.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Bucket standardRateLimit;
    private final Bucket authRateLimit;
    private final ObjectMapper objectMapper;

    public RateLimitingFilter(
            @Qualifier("standardRateLimit") Bucket standardRateLimit,
            @Qualifier("authRateLimit") Bucket authRateLimit,
            ObjectMapper objectMapper) {
        this.standardRateLimit = standardRateLimit;
        this.authRateLimit = authRateLimit;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        Bucket bucketToUse;
        
        // Use stricter rate limits for authentication endpoints
        if (path.startsWith("/api/auth")) {
            bucketToUse = authRateLimit;
        } else {
            bucketToUse = standardRateLimit;
        }
        
        // Try to consume a token
        if (bucketToUse.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            // Return a 429 Too Many Requests response
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            
            ApiResponse<?> apiResponse = new ApiResponse<>(
                false,
                "Rate limit exceeded. Please try again later.",
                null,
                LocalDateTime.now()
            );
            
            response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        }
    }
} 