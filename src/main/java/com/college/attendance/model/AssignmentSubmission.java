package com.college.attendance.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "assignment_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class AssignmentSubmission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    @JsonBackReference
    private Assignment assignment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIdentityReference(alwaysAsId = true)
    private User student;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(nullable = false)
    private LocalDateTime submissionDate = LocalDateTime.now();
    
    private LocalDateTime gradedDate;
    
    private Integer score;
    
    @Column(columnDefinition = "TEXT")
    private String feedback;
    
    @ElementCollection
    @CollectionTable(name = "submission_files", joinColumns = @JoinColumn(name = "submission_id"))
    private List<FileInfo> files;
    
    @Column(nullable = false)
    private boolean graded = false;
    
    @Column(nullable = false)
    private boolean late = false;
    
    // New JSON properties for client consumption
    @JsonProperty("studentName")
    public String getStudentName() {
        return student != null ? student.getFullName() : null;
    }

    @JsonProperty("assignmentId")
    public Long getAssignmentId() {
        return assignment != null ? assignment.getId() : null;
    }

    @JsonProperty("assignmentTitle")
    public String getAssignmentTitle() {
        return assignment != null ? assignment.getTitle() : null;
    }
}