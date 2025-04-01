package com.college.attendance.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for logging execution of service and repository Spring components.
 */
@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Pointcut that matches all repositories, services, and controllers.
     */
    @Pointcut("within(@org.springframework.stereotype.Repository *)" +
            " || within(@org.springframework.stereotype.Service *)" +
            " || within(@org.springframework.web.bind.annotation.RestController *)")
    public void springBeanPointcut() {
        // Method is empty as this is just a Pointcut definition
    }

    /**
     * Pointcut that matches all classes within the application package.
     */
    @Pointcut("within(com.college.attendance..*)")
    public void applicationPackagePointcut() {
        // Method is empty as this is just a Pointcut definition
    }

    /**
     * Logs methods throwing exceptions.
     *
     * @param joinPoint join point for advice
     * @param e exception
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        log.error("Exception in {}.{}() with cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(), e.getCause() != null ? e.getCause() : "NULL");
        
        // Log the stack trace for detailed debugging
        if (log.isDebugEnabled()) {
            log.debug("Exception stacktrace: ", e);
        }
    }

    /**
     * Logs method execution.
     *
     * @param joinPoint join point for advice
     * @return result
     * @throws Throwable throws Exception
     */
    @Around("applicationPackagePointcut() && springBeanPointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (log.isDebugEnabled()) {
            log.debug("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
        }
        
        try {
            // Start timing
            long start = System.currentTimeMillis();
            
            // Execute the method
            Object result = joinPoint.proceed();
            
            // End timing
            long executionTime = System.currentTimeMillis() - start;
            
            // Log the result but be careful with large objects
            if (log.isDebugEnabled()) {
                log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(), result);
            }
            
            // Always log execution time if it exceeds threshold
            if (executionTime > 500) {
                log.warn("Long execution time: {}.{}() took {}ms", joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(), executionTime);
            } else if (log.isDebugEnabled()) {
                log.debug("Execution time: {}.{}() took {}ms", joinPoint.getSignature().getDeclaringTypeName(),
                        joinPoint.getSignature().getName(), executionTime);
            }
            
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
                    joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            throw e;
        }
    }
} 