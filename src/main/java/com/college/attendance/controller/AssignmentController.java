package com.college.attendance.controller;

import com.college.attendance.dto.AssignmentDto;
import com.college.attendance.dto.GradingDto;
import com.college.attendance.dto.SubmissionDto;
import com.college.attendance.model.*;
import com.college.attendance.repository.AssignmentRepository;
import com.college.attendance.repository.AssignmentSubmissionRepository;
import com.college.attendance.repository.CourseRepository;
import com.college.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {
    
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<Assignment>> createAssignment(@Valid @RequestBody AssignmentDto assignmentDto) {
        try {
            // Get the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Get the course
            Course course = courseRepository.findById(assignmentDto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
            
            // Create and save the assignment
            Assignment assignment = new Assignment();
            assignment.setTitle(assignmentDto.getTitle());
            assignment.setDescription(assignmentDto.getDescription());
            assignment.setCourse(course);
            assignment.setCreator(creator);
            assignment.setDueDate(assignmentDto.getDueDate());
            if (assignmentDto.getMaxPoints() != null) {
                assignment.setMaxPoints(assignmentDto.getMaxPoints());
            }
            // Handle isDraft field
            if (assignmentDto.getIsDraft() != null) {
                assignment.setDraft(assignmentDto.getIsDraft());
            }
            
            if (assignmentDto.getFiles() != null && !assignmentDto.getFiles().isEmpty()) {
                List<FileInfo> files = assignmentDto.getFiles().stream()
                    .map(fileDto -> new FileInfo(
                        fileDto.getFileName(),
                        fileDto.getFileUrl(),
                        fileDto.getContentType(),
                        fileDto.getFileSize()
                    ))
                    .collect(Collectors.toList());
                assignment.setFiles(files);
            }
            
            Assignment savedAssignment = assignmentRepository.save(assignment);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Assignment created successfully", savedAssignment)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Failed to create assignment: " + e.getMessage())
            );
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Assignment>>> getAssignmentsByCourse(
            @RequestParam Long courseId) {
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        
        List<Assignment> assignments = assignmentRepository.findByCourse(course);
        
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }
    
    @GetMapping("/all")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<List<Assignment>>> getAllAssignments(
            @RequestParam Long courseId) {
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        
        List<Assignment> assignments = assignmentRepository.findByCourse(course);
        
        return ResponseEntity.ok(ApiResponse.success(assignments));
    }
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<Assignment>>> getActiveAssignments(
            @RequestParam Long courseId) {
        try {
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
            
            LocalDateTime now = LocalDateTime.now();
            
            List<Assignment> assignments = assignmentRepository.findByCourseAndDueDateAfter(course, now);
            
            // Handle circular references by removing potentially problematic references
            assignments.forEach(assignment -> {
                // Break circular references
                if (assignment.getSubmissions() != null) {
                    assignment.getSubmissions().forEach(submission -> {
                        submission.setAssignment(null);
                    });
                }
            });
            
            return ResponseEntity.ok(ApiResponse.success(assignments));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error fetching active assignments: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse<Assignment>> getAssignmentById(
            @PathVariable Long assignmentId) {
        
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        
        return ResponseEntity.ok(ApiResponse.success(assignment));
    }
    
    @PostMapping("/{assignmentId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AssignmentSubmission>> submitAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmissionDto submissionDto) {
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User student = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get the assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        
        // Check if the due date has passed
        LocalDateTime now = LocalDateTime.now();
        boolean isLate = now.isAfter(assignment.getDueDate());
        
        // Create the submission
        AssignmentSubmission submission = new AssignmentSubmission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setNotes(submissionDto.getNotes());
        submission.setSubmissionDate(now);
        submission.setGraded(false);
        submission.setLate(isLate);
        
        // Process file attachments if any
        if (submissionDto.getFiles() != null && !submissionDto.getFiles().isEmpty()) {
            List<FileInfo> files = submissionDto.getFiles().stream()
                .map(fileDto -> {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFileName(fileDto.getFileName());
                    fileInfo.setFileUrl(fileDto.getFileUrl());
                    fileInfo.setContentType(fileDto.getContentType());
                    fileInfo.setFileSize(fileDto.getFileSize());
                    fileInfo.setUploadedAt(LocalDateTime.now());
                    return fileInfo;
                })
                .collect(Collectors.toList());
            
            submission.setFiles(files);
        }
        
        AssignmentSubmission savedSubmission = submissionRepository.save(submission);
        
        return ResponseEntity.ok(
            ApiResponse.success(
                isLate ? "Assignment submitted late" : "Assignment submitted successfully",
                savedSubmission
            )
        );
    }
    
    @PutMapping("/submissions/{submissionId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<AssignmentSubmission>> editSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody SubmissionDto submissionDto) {
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User student = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get the submission
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        
        // Check if this is the student's submission
        if (!submission.getStudent().getId().equals(student.getId())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("You can only edit your own submissions"));
        }
        
        // Get the assignment
        Assignment assignment = submission.getAssignment();
        
        // Check if the due date has passed
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(assignment.getDueDate())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Cannot edit submission after due date"));
        }
        
        // Update submission details
        submission.setNotes(submissionDto.getNotes());
        submission.setSubmissionDate(now); // Update submission date to reflect the edit
        
        // If previously graded, mark it to indicate it was edited after grading
        if (submission.isGraded()) {
            // We're not changing the graded status, but the updated submission date 
            // will be more recent than the graded date, which will allow the frontend 
            // to detect this case
        }
        
        // Process file attachments if any
        if (submissionDto.getFiles() != null && !submissionDto.getFiles().isEmpty()) {
            List<FileInfo> files = submissionDto.getFiles().stream()
                .map(fileDto -> {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFileName(fileDto.getFileName());
                    fileInfo.setFileUrl(fileDto.getFileUrl());
                    fileInfo.setContentType(fileDto.getContentType());
                    fileInfo.setFileSize(fileDto.getFileSize());
                    fileInfo.setUploadedAt(LocalDateTime.now());
                    return fileInfo;
                })
                .collect(Collectors.toList());
            
            submission.setFiles(files);
        }
        
        AssignmentSubmission savedSubmission = submissionRepository.save(submission);
        
        return ResponseEntity.ok(
            ApiResponse.success("Assignment submission updated successfully", savedSubmission)
        );
    }
    
    @GetMapping("/{assignmentId}/submissions")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<List<AssignmentSubmission>>> getSubmissions(
            @PathVariable Long assignmentId) {
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get the assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        
        // Check if the professor created this assignment
        if (!assignment.getCreator().getId().equals(professor.getId())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("You can only view submissions for assignments you created"));
        }
        
        List<AssignmentSubmission> submissions = submissionRepository.findByAssignment(assignment);
        
        return ResponseEntity.ok(ApiResponse.success(submissions));
    }
    
    @GetMapping("/submissions/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<AssignmentSubmission>>> getStudentSubmissions() {
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User student = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<AssignmentSubmission> submissions = submissionRepository.findByStudent(student);
        
        return ResponseEntity.ok(ApiResponse.success(submissions));
    }
    
    @PostMapping("/submissions/{submissionId}/grade")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<AssignmentSubmission>> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradingDto gradingDto) {
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get the submission
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        
        // Check if the professor created the assignment
        if (!submission.getAssignment().getCreator().getId().equals(professor.getId())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("You can only grade submissions for assignments you created"));
        }
        
        // Update the submission with grading info
        submission.setScore(gradingDto.getScore());
        submission.setFeedback(gradingDto.getFeedback());
        submission.setGraded(true);
        submission.setGradedDate(LocalDateTime.now());
        
        AssignmentSubmission savedSubmission = submissionRepository.save(submission);
        
        return ResponseEntity.ok(
            ApiResponse.success("Submission graded successfully", savedSubmission)
        );
    }
    
    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<String>> deleteAssignment(
            @PathVariable Long assignmentId) {
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get the assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        
        // Check if the professor created this assignment
        if (!assignment.getCreator().getId().equals(professor.getId())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("You can only delete assignments you created"));
        }
        
        assignmentRepository.delete(assignment);
        
        return ResponseEntity.ok(
            ApiResponse.success("Assignment deleted successfully")
        );
    }
    
    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<Assignment>> updateAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody AssignmentDto assignmentDto) {
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get the assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        
        // Check if the professor created this assignment
        if (!assignment.getCreator().getId().equals(professor.getId())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("You can only update assignments you created"));
        }
        
        // Update the assignment
        assignment.setTitle(assignmentDto.getTitle());
        assignment.setDescription(assignmentDto.getDescription());
        assignment.setDueDate(assignmentDto.getDueDate());
        assignment.setMaxPoints(assignmentDto.getMaxPoints());
        
        // Process file attachments if any
        if (assignmentDto.getFiles() != null && !assignmentDto.getFiles().isEmpty()) {
            List<FileInfo> files = assignmentDto.getFiles().stream()
                .map(fileDto -> {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFileName(fileDto.getFileName());
                    fileInfo.setFileUrl(fileDto.getFileUrl());
                    fileInfo.setContentType(fileDto.getContentType());
                    fileInfo.setFileSize(fileDto.getFileSize());
                    fileInfo.setUploadedAt(LocalDateTime.now());
                    return fileInfo;
                })
                .collect(Collectors.toList());
            
            assignment.setFiles(files);
        }
        
        Assignment updatedAssignment = assignmentRepository.save(assignment);
        
        return ResponseEntity.ok(
            ApiResponse.success("Assignment updated successfully", updatedAssignment)
        );
    }
    
    // Get draft assignments (Professor only)
    @GetMapping("/drafts")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<List<Assignment>>> getDraftAssignments() {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get all assignments by this professor with isDraft=true
        List<Assignment> draftAssignments = assignmentRepository.findByCreatorAndIsDraftTrue(professor);
        
        return ResponseEntity.ok(
            ApiResponse.success("Draft assignments retrieved successfully", draftAssignments)
        );
    }
}