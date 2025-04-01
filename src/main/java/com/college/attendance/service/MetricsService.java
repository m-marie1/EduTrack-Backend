package com.college.attendance.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Service for tracking application metrics using Micrometer
 */
@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // Counter metrics
    private final Counter totalLoginAttempts;
    private final Counter successfulLogins;
    private final Counter failedLogins;
    private final Counter totalAttendanceRecords;
    private final Counter totalFileUploads;
    private final Counter apiRequests;
    
    // Timer metrics
    private final Timer attendanceRecordTimer;
    private final Timer fileUploadTimer;
    private final Timer quizSubmissionTimer;
    private final Timer assignmentSubmissionTimer;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Initialize counters
        this.totalLoginAttempts = Counter.builder("app.logins.attempts")
                .description("Total number of login attempts")
                .register(meterRegistry);
        
        this.successfulLogins = Counter.builder("app.logins.success")
                .description("Number of successful logins")
                .register(meterRegistry);
        
        this.failedLogins = Counter.builder("app.logins.failed")
                .description("Number of failed logins")
                .register(meterRegistry);
        
        this.totalAttendanceRecords = Counter.builder("app.attendance.records")
                .description("Total number of attendance records")
                .register(meterRegistry);
        
        this.totalFileUploads = Counter.builder("app.uploads.count")
                .description("Total number of file uploads")
                .register(meterRegistry);
        
        this.apiRequests = Counter.builder("app.api.requests")
                .description("Total number of API requests")
                .register(meterRegistry);
        
        // Initialize timers
        this.attendanceRecordTimer = Timer.builder("app.attendance.duration")
                .description("Time taken to record attendance")
                .register(meterRegistry);
        
        this.fileUploadTimer = Timer.builder("app.uploads.duration")
                .description("Time taken to upload files")
                .register(meterRegistry);
        
        this.quizSubmissionTimer = Timer.builder("app.quiz.submission.duration")
                .description("Time taken to submit quizzes")
                .register(meterRegistry);
        
        this.assignmentSubmissionTimer = Timer.builder("app.assignment.submission.duration")
                .description("Time taken to submit assignments")
                .register(meterRegistry);
    }

    // Counter methods
    public void incrementLoginAttempt() {
        totalLoginAttempts.increment();
    }

    public void incrementSuccessfulLogin() {
        successfulLogins.increment();
    }

    public void incrementFailedLogin() {
        failedLogins.increment();
    }

    public void incrementAttendanceRecord() {
        totalAttendanceRecords.increment();
    }

    public void incrementFileUpload() {
        totalFileUploads.increment();
    }

    public void incrementApiRequest() {
        apiRequests.increment();
    }

    // Timer methods
    public void recordAttendanceDuration(long milliseconds) {
        attendanceRecordTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public void recordFileUploadDuration(long milliseconds) {
        fileUploadTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public void recordQuizSubmissionDuration(long milliseconds) {
        quizSubmissionTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    public void recordAssignmentSubmissionDuration(long milliseconds) {
        assignmentSubmissionTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Records execution time of a supplier function
     * @param timer Timer to record against
     * @param supplier Function to execute and time
     * @return Result of the supplier function
     */
    public <T> T recordTime(Timer timer, Supplier<T> supplier) {
        return timer.record(supplier);
    }

    /**
     * Creates or gets a counter with tags
     * @param name Counter name
     * @param tags Tags as key-value pairs (must be even number of arguments)
     * @return The counter
     */
    public Counter counter(String name, String... tags) {
        return Counter.builder(name)
                .tags(tags)
                .register(meterRegistry);
    }

    /**
     * Creates or gets a timer with tags
     * @param name Timer name
     * @param tags Tags as key-value pairs (must be even number of arguments)
     * @return The timer
     */
    public Timer timer(String name, String... tags) {
        return Timer.builder(name)
                .tags(tags)
                .register(meterRegistry);
    }
} 