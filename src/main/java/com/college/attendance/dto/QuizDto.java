package com.college.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizDto {
    
    private Long id;
    
    @NotBlank(message = "Quiz title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Course ID is required")
    private Long courseId;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    
    @NotNull(message = "Duration in minutes is required")
    @Positive(message = "Duration must be positive")
    private Integer durationMinutes;
    
    @Size(min = 1, message = "At least one question is required")
    private List<QuestionDto> questions;
} 