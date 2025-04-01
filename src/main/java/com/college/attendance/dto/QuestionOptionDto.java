package com.college.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionDto {
    
    private Long id;
    
    @NotBlank(message = "Option text is required")
    private String text;
    
    @NotNull(message = "Correctness must be specified")
    private Boolean correct;
    
    private Integer order;
} 