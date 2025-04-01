package com.college.attendance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "professor_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String idImageUrl;
    
    @Column(nullable = false)
    private String department;
    
    private String additionalInfo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;
    
    @Column(nullable = false)
    private LocalDateTime requestDate = LocalDateTime.now();
    
    private LocalDateTime reviewDate;
    
    private String reviewedBy;
    
    private String rejectionReason;
    
    @OneToOne(mappedBy = "professorRequest", cascade = CascadeType.ALL)
    private User user;
} 