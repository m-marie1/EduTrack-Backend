package com.college.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDto {
    
    @NotNull(message = "Assignment ID is required")
    private Long assignmentId;
    
    private String notes;
    
    private List<FileDto> files;
} 