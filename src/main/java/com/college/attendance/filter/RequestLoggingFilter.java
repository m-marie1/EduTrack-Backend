package com.college.attendance.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Filter for logging HTTP requests and responses
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final int MAX_PAYLOAD_LENGTH = 10000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip actuator endpoints to avoid excessive logging
        if (request.getRequestURI().startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Wrap request and response to capture content
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        // Capture request details before processing
        String requestId = java.util.UUID.randomUUID().toString();
        String requestMethod = request.getMethod();
        String requestUri = request.getRequestURI();
        
        try {
            // Record start time
            long startTime = System.currentTimeMillis();
            
            // Process the request
            filterChain.doFilter(requestWrapper, responseWrapper);
            
            // Calculate request duration
            long duration = System.currentTimeMillis() - startTime;
            
            // Log request and response after processing
            logRequest(requestWrapper, requestId);
            logResponse(responseWrapper, requestId, duration);
            
        } finally {
            // Copy content back to response
            responseWrapper.copyBodyToResponse();
        }
    }
    
    private void logRequest(ContentCachingRequestWrapper request, String requestId) {
        Map<String, Object> details = new HashMap<>();
        details.put("requestId", requestId);
        details.put("method", request.getMethod());
        details.put("uri", request.getRequestURI());
        details.put("query", request.getQueryString());
        details.put("remote", request.getRemoteAddr());
        details.put("headers", getRequestHeaders(request));
        
        // Only log request body for non-GET requests
        if (!request.getMethod().equals("GET")) {
            String payload = getRequestPayload(request);
            if (payload != null && !payload.isEmpty()) {
                details.put("body", payload);
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Request: {}", details);
        } else {
            // Simplified log for INFO level
            log.info("Request {} {} - {}?{}", requestId, request.getMethod(), request.getRequestURI(), 
                    request.getQueryString() != null ? request.getQueryString() : "");
        }
    }
    
    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Skip sensitive headers
            if (!"authorization".equalsIgnoreCase(headerName) && 
                !"cookie".equalsIgnoreCase(headerName)) {
                headers.put(headerName, request.getHeader(headerName));
            } else {
                headers.put(headerName, "[REDACTED]");
            }
        }
        return headers;
    }
    
    private String getRequestPayload(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            int length = Math.min(content.length, MAX_PAYLOAD_LENGTH);
            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                return new String(content, 0, length, StandardCharsets.UTF_8)
                        .replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"[REDACTED]\"");
            } else {
                return String.format("[%d bytes of %s content]", content.length, contentType);
            }
        }
        return "";
    }
    
    private void logResponse(ContentCachingResponseWrapper response, String requestId, long duration) {
        Map<String, Object> details = new HashMap<>();
        details.put("requestId", requestId);
        details.put("status", response.getStatus());
        details.put("duration", duration);
        details.put("headers", getResponseHeaders(response));
        
        // Always log response status
        log.info("Response {} status: {} - {}ms", requestId, response.getStatus(), duration);
        
        // Log detailed response only in debug mode
        if (log.isDebugEnabled()) {
            String payload = getResponsePayload(response);
            if (payload != null && !payload.isEmpty()) {
                details.put("body", payload);
            }
            log.debug("Response details: {}", details);
        }
    }
    
    private Map<String, String> getResponseHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        Collection<String> headerNames = response.getHeaderNames();
        for (String headerName : headerNames) {
            headers.put(headerName, response.getHeader(headerName));
        }
        return headers;
    }
    
    private String getResponsePayload(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            int length = Math.min(content.length, MAX_PAYLOAD_LENGTH);
            String contentType = response.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                return new String(content, 0, length, StandardCharsets.UTF_8);
            } else {
                return String.format("[%d bytes of %s content]", content.length, contentType);
            }
        }
        return "";
    }
} 