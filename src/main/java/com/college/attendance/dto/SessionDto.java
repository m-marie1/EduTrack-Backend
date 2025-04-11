package com.college.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {

    private Long sessionId;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String verificationCode; // The generated code for students
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean active;
}