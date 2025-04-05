package com.college.attendance.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Represents a class session or lecture given by a professor for a course.
 * This is used to track attendance for specific class meetings.
 */
@Entity
@Table(name = "class_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The course this class session belongs to
     */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    /**
     * The professor who conducted this class session
     */
    @ManyToOne
    @JoinColumn(name = "professor_id", nullable = false)
    private User professor;
    
    /**
     * The date of the class session
     */
    @Column(nullable = false)
    private LocalDate date;
    
    /**
     * Start time of the class
     */
    @Column(nullable = false)
    private LocalTime startTime;
    
    /**
     * End time of the class
     */
    @Column(nullable = false)
    private LocalTime endTime;
    
    /**
     * Topic or material covered in this session
     */
    private String topic;
    
    /**
     * Attendance records for this class session
     */
    @OneToMany(mappedBy = "classSession", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<Attendance> attendanceRecords;
    
    /**
     * Verification codes generated for this session
     */
    @OneToMany(mappedBy = "classSession", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<VerificationCode> verificationCodes;
} 