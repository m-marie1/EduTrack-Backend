package com.college.attendance.dto;

import com.college.attendance.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    
    private Long id;
    
    @NotBlank(message = "Question text is required")
    private String text;
    
    private String imageUrl;
    
    @NotNull(message = "Question type is required")
    private QuestionType type;
    
    private Integer points = 1;
    
    private Integer order;
    
    // For multiple choice questions
    @Size(min = 2, message = "At least two options are required for multiple-choice questions")
    private List<QuestionOptionDto> options;
    
    // For text answer questions
    private String correctAnswer;
} 