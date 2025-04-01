package com.college.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAnswerDto {
    
    @NotNull(message = "Question ID is required")
    private Long questionId;
    
    // For multiple-choice questions
    private Long selectedOptionId;
    
    // For text answers
    private String textAnswer;
} 