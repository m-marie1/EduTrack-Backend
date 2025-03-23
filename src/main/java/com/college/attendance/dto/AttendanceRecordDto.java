package com.college.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordDto {
    
    @NotNull
    private Long courseId;
    
    private String networkIdentifier;
    
    private String verificationMethod;
}