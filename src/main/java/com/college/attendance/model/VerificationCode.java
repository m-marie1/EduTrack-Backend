package com.college.attendance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a verification code used for recording student attendance.
 * Generated by professors when they record a class session.
 */
@Entity
@Table(name = "verification_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The actual verification code (6-digit number)
     */
    @Column(nullable = false, unique = true)
    private String code;
    
    /**
     * The class session this verification code is for
     */
    @ManyToOne
    @JoinColumn(name = "class_session_id", nullable = false)
    private ClassSession classSession;
    
    /**
     * When this code expires (typically 15 minutes after creation)
     */
    @Column(nullable = false)
    private LocalDateTime expiryTime;
    
    /**
     * Whether this code has been used
     */
    private boolean used = false;
    
    /**
     * When this code was created
     */
    private LocalDateTime createdAt = LocalDateTime.now();
} 