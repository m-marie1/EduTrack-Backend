package com.college.attendance.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Student who attended the class
     */
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    /**
     * The class session this attendance record is for
     */
    @ManyToOne
    @JoinColumn(name = "class_session_id", nullable = false)
    private ClassSession classSession;
    
    /**
     * When the attendance was recorded
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    /**
     * Method used to verify attendance (WiFi, QR, CODE, GPS)
     */
    private String verificationMethod;
    
    /**
     * Whether the attendance is verified as valid
     */
    private boolean verified = false;
    
    /**
     * For legacy support: the course this attendance is for
     * This is redundant with classSession.course, but kept for backward compatibility
     */
    @ManyToOne
    @JoinColumn(name = "course_id")
    @JsonBackReference
    private Course course;
    
    /**
     * For legacy support: the network identifier used (e.g., WiFi SSID)
     */
    private String networkIdentifier;
} 