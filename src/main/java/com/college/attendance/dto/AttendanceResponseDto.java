package com.college.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponseDto {
    
    private Long id;
    private String studentName;
    private String studentId;
    private String courseCode;
    private String courseName;
    private LocalDateTime timestamp;
    private boolean verified;
    private String verificationMethod;
}