package com.college.attendance.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DetailedRequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        // Generate a unique ID for this request
        String requestId = UUID.randomUUID().toString();
        
        // Wrap request and response for content caching
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Log request details before processing
            logRequestDetails(requestId, requestWrapper);
            
            // Continue with the filter chain
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // Log response details after processing
            logResponseDetails(requestId, responseWrapper, System.currentTimeMillis() - startTime);
            
            // Copy content from response wrapper to original response
            responseWrapper.copyBodyToResponse();
        }
    }
    
    private void logRequestDetails(String requestId, ContentCachingRequestWrapper request) {
        // Log request method, URL, headers, and parameters
        String headers = Collections.list(request.getHeaderNames())
                .stream()
                .map(headerName -> headerName + ":" + request.getHeader(headerName))
                .collect(Collectors.joining(", "));
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n=========================== REQUEST BEGIN ===========================\n");
        sb.append("ID: ").append(requestId).append("\n");
        sb.append("Method: ").append(request.getMethod()).append("\n");
        sb.append("URI: ").append(request.getRequestURI());
        if (request.getQueryString() != null) {
            sb.append("?").append(request.getQueryString());
        }
        sb.append("\n");
        sb.append("Headers: ").append(headers).append("\n");
        
        // Log parameters
        Enumeration<String> paramNames = request.getParameterNames();
        if (paramNames.hasMoreElements()) {
            sb.append("Parameters:\n");
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                String paramValue = request.getParameter(paramName);
                sb.append("  ").append(paramName).append("=").append(paramValue).append("\n");
            }
        }
        
        // Log request body if available (only for content types that typically include a request body)
        String contentType = request.getContentType();
        if (contentType != null && (contentType.contains("application/json") || 
                                   contentType.contains("application/xml") || 
                                   contentType.contains("text/plain") ||
                                   contentType.contains("application/x-www-form-urlencoded"))) {
            // Get request body
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);
                sb.append("Body: ").append(body).append("\n");
            }
        }
        
        sb.append("=========================== REQUEST END ===========================\n");
        log.info(sb.toString());
    }
    
    private void logResponseDetails(String requestId, ContentCachingResponseWrapper response, long executionTime) {
        // Log response status, execution time, and headers
        StringBuilder sb = new StringBuilder();
        sb.append("\n=========================== RESPONSE BEGIN ===========================\n");
        sb.append("ID: ").append(requestId).append("\n");
        sb.append("Status: ").append(response.getStatus()).append("\n");
        sb.append("Time: ").append(executionTime).append("ms\n");
        
        // Log headers
        String headers = response.getHeaderNames()
                .stream()
                .map(headerName -> headerName + ":" + response.getHeader(headerName))
                .collect(Collectors.joining(", "));
        sb.append("Headers: ").append(headers).append("\n");
        
        // Log response body if available
        String contentType = response.getContentType();
        if (contentType != null && (contentType.contains("application/json") || 
                                   contentType.contains("application/xml") || 
                                   contentType.contains("text/plain"))) {
            byte[] content = response.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);
                sb.append("Body: ").append(body).append("\n");
            }
        }
        
        sb.append("=========================== RESPONSE END ===========================\n");
        log.info(sb.toString());
    }
} 