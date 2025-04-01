package com.college.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    
    @NotNull(message = "Approval status is required")
    private Boolean approved;
    
    private String rejectionReason;
    
    @NotBlank(message = "Reviewer username is required")
    private String reviewedBy;
} 