package com.college.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for recording a class session by a professor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassRecordRequest {
    
    /**
     * ID of the course for which the class is being recorded
     */
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    /**
     * Date of the class session
     */
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    /**
     * Start time of the class
     */
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    /**
     * End time of the class
     */
    @NotNull(message = "End time is required")
    private LocalTime endTime;
    
    /**
     * Topic covered in the class
     */
    private String topic;
} 