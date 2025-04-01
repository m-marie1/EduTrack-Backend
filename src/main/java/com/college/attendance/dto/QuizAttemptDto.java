package com.college.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptDto {
    
    @NotNull(message = "Quiz ID is required")
    private Long quizId;
    
    private List<QuizAnswerDto> answers;
} 