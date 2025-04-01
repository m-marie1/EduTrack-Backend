package com.college.attendance.exception;

import com.college.attendance.controller.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle custom ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        logException(ex, request, HttpStatus.NOT_FOUND);
        
        ApiResponse<?> response = new ApiResponse<>(
            false,
            ex.getMessage(),
            null,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle custom InvalidOperationException
     */
    @ExceptionHandler(InvalidOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleInvalidOperationException(InvalidOperationException ex, WebRequest request) {
        logException(ex, request, HttpStatus.BAD_REQUEST);
        
        ApiResponse<?> response = new ApiResponse<>(
            false,
            ex.getMessage(),
            null,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logException(ex, request, HttpStatus.BAD_REQUEST);
        
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing + ", " + replacement
                ));

        ApiResponse<?> response = new ApiResponse<>(
            false,
            "Validation error",
            errors,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        logException(ex, request, HttpStatus.BAD_REQUEST);
        
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String path = violation.getPropertyPath().toString();
            String field = path.substring(path.lastIndexOf('.') + 1);
            errors.put(field, violation.getMessage());
        });

        ApiResponse<?> response = new ApiResponse<>(
            false,
            "Validation error",
            errors,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleMissingParams(
            MissingServletRequestParameterException ex, WebRequest request) {
        
        logException(ex, request, HttpStatus.BAD_REQUEST);
        
        ApiResponse<?> response = new ApiResponse<>(
            false,
            "Missing required parameter: " + ex.getParameterName(),
            null,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler({
        BadCredentialsException.class,
        DisabledException.class,
        LockedException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(
            Exception ex, WebRequest request) {
        
        logException(ex, request, HttpStatus.UNAUTHORIZED);
        
        String message = "Authentication failed";
        if (ex instanceof BadCredentialsException) {
            message = "Invalid username or password";
        } else if (ex instanceof DisabledException) {
            message = "Account is disabled";
        } else if (ex instanceof LockedException) {
            message = "Account is locked";
        }

        ApiResponse<?> response = new ApiResponse<>(
            false,
            message,
            null,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        logException(ex, request, HttpStatus.FORBIDDEN);
        
        ApiResponse<?> response = new ApiResponse<>(
            false,
            "Access denied",
            null,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Handle JWT exceptions
     */
    @ExceptionHandler({
        ExpiredJwtException.class,
        UnsupportedJwtException.class,
        MalformedJwtException.class,
        SignatureException.class,
        IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ApiResponse<?>> handleJwtException(Exception ex, WebRequest request) {
        logException(ex, request, HttpStatus.UNAUTHORIZED);
        
        String message = "Invalid token";
        if (ex instanceof ExpiredJwtException) {
            message = "Token has expired";
        } else if (ex instanceof UnsupportedJwtException) {
            message = "Unsupported JWT token";
        } else if (ex instanceof MalformedJwtException) {
            message = "Invalid JWT token";
        } else if (ex instanceof SignatureException) {
            message = "Invalid JWT signature";
        } else if (ex instanceof IllegalArgumentException) {
            message = "JWT claims string is empty";
        }

        ApiResponse<?> response = new ApiResponse<>(
            false,
            message,
            null,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handle business logic exceptions
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {
        
        logException(ex, request, HttpStatus.BAD_REQUEST);
        
        ApiResponse<?> response = new ApiResponse<>(
            false,
            ex.getMessage(),
            null,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle file size exceeding limit
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<?>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, WebRequest request) {
        
        logException(ex, request, HttpStatus.BAD_REQUEST);
        
        ApiResponse<?> response = new ApiResponse<>(
            false,
            "File size exceeds the maximum allowed size",
            null,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle database integrity violations
     */
    @ExceptionHandler({
        DataIntegrityViolationException.class,
        SQLException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<?>> handleDatabaseException(Exception ex, WebRequest request) {
        logException(ex, request, HttpStatus.CONFLICT);
        
        String message = "Database error";
        if (ex.getMessage().contains("duplicate key") || ex.getMessage().contains("Duplicate entry")) {
            message = "Data already exists";
        }

        ApiResponse<?> response = new ApiResponse<>(
            false,
            message,
            null,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<?>> handleAllExceptions(Exception ex, WebRequest request) {
        // Log full stack trace for unhandled exceptions
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        
        ApiResponse<?> response = new ApiResponse<>(
            false,
            "An unexpected error occurred",
            null,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Log exception details
     */
    private void logException(Exception ex, WebRequest request, HttpStatus status) {
        String path = request instanceof ServletWebRequest ? 
                ((ServletWebRequest) request).getRequest().getRequestURI() : "unknown";
        
        log.error("Exception: {} {} - {} - {}", 
                status.value(), 
                status.getReasonPhrase(), 
                path, 
                ex.getMessage());
        
        if (log.isDebugEnabled()) {
            log.debug("Exception details:", ex);
        }
    }
}