package com.college.attendance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String fileUrl;
    
    @Column(nullable = false)
    private String contentType;
    
    private Long fileSize;
    
    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();
} 