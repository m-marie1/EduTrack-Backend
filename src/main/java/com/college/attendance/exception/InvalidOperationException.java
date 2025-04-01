package com.college.attendance.exception;

/**
 * Exception thrown when an operation is attempted but cannot be performed
 * due to the current state of the system or invalid parameters.
 */
public class InvalidOperationException extends RuntimeException {
    
    public InvalidOperationException(String message) {
        super(message);
    }
    
    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
} 