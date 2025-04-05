package com.college.attendance.controller;

import com.college.attendance.dto.AttendanceRecordDto;
import com.college.attendance.dto.AttendanceResponseDto;
import com.college.attendance.exception.ResourceNotFoundException;
import com.college.attendance.model.Course;
import com.college.attendance.model.User;
import com.college.attendance.repository.UserRepository;
import com.college.attendance.service.AttendanceService;
import com.college.attendance.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    
    private final AttendanceService attendanceService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    
    // @PostMapping("/record")
    // public ResponseEntity<ApiResponse<AttendanceResponseDto>> recordAttendance(
    //         @Valid @RequestBody AttendanceRecordDto attendanceDto) {
        
    //     // Get the authenticated user
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     String username = authentication.getName();
    //     User user = userRepository.findByUsername(username)
    //         .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
    //     AttendanceResponseDto response = attendanceService.recordAttendance(user, attendanceDto);
        
    //     return ResponseEntity.ok(ApiResponse.success("Attendance recorded successfully", response));
    // }

    @PostMapping("/record")
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> recordAttendance(
            @Valid @RequestBody AttendanceRecordDto attendanceDto) {
        try {
            // Get the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            AttendanceResponseDto response = attendanceService.recordAttendance(user, attendanceDto);
            
            return ResponseEntity.ok(ApiResponse.success("Attendance recorded successfully", response));
        } catch (IllegalStateException e) {
            // This is for business logic exceptions (not enrolled, not in session, etc.)
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Error recording attendance: " + e.getMessage()));
        }
    }

    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<ApiResponse<String>> enrollInCourse(@PathVariable Long courseId) {
        try {
            // Get the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Delegate to service layer
            String result = attendanceService.enrollUserInCourse(user, courseId);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                ApiResponse.error("Enrollment failed: " + e.getMessage())
            );
        }
    }
    
    @GetMapping("/user/{userId}/course/{courseId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getUserAttendanceForCourse(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        
        List<AttendanceResponseDto> attendanceList = 
            attendanceService.getUserAttendanceForCourse(userId, courseId);
        
        return ResponseEntity.ok(ApiResponse.success(attendanceList));
    }
    
    @GetMapping("/user/username/{username}/course/{courseId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getUserAttendanceForCourseByUsername(
            @PathVariable String username,
            @PathVariable Long courseId) {
        try {
            // Find the user by username
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
                
            List<AttendanceResponseDto> attendanceList = 
                attendanceService.getUserAttendanceForCourse(user.getId(), courseId);
            
            return ResponseEntity.ok(ApiResponse.success(attendanceList));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Error retrieving attendance: " + e.getMessage()));
        }
    }
    
    @GetMapping("/user/current/course/{courseId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getCurrentUserAttendanceForCourse(
            @PathVariable Long courseId) {
        try {
            // Get the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
            List<AttendanceResponseDto> attendanceList = 
                attendanceService.getUserAttendanceForCourse(user.getId(), courseId);
            
            return ResponseEntity.ok(ApiResponse.success(attendanceList));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("Error retrieving attendance: " + e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}/date/{date}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getUserAttendanceForDate(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<AttendanceResponseDto> attendanceList = 
            attendanceService.getUserAttendanceForDate(userId, date);
        
        return ResponseEntity.ok(ApiResponse.success(attendanceList));
    }
    
    @GetMapping("/course/{courseId}/date/{date}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getCourseAttendanceForDate(
            @PathVariable Long courseId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<AttendanceResponseDto> attendanceList = 
            attendanceService.getCourseAttendanceForDate(courseId, date);
        
        return ResponseEntity.ok(ApiResponse.success(attendanceList));
    }
    
    @GetMapping("/user/{userId}/course/{courseId}/present")
    public ResponseEntity<ApiResponse<Boolean>> isUserPresentToday(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        
        boolean isPresent = attendanceService.isUserPresentForCourseToday(userId, courseId);
        
        return ResponseEntity.ok(ApiResponse.success(
            isPresent ? "Student is present today" : "Student is not present today", 
            isPresent));
    }

    /**
     * Record a class session (professor only)
     */
    @PostMapping("/classes/record")
    public ResponseEntity<ApiResponse<ClassSession>> recordClass(
            @RequestBody ClassRecordRequest recordRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Get the professor
            User professor = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Check if user is a professor
            if (professor.getRole() != Role.PROFESSOR) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Only professors can record classes"));
            }
            
            // Get the course
            Course course = courseRepository.findById(recordRequest.getCourseId())
                    .orElseThrow(() -> new IllegalArgumentException("Course not found"));
            
            // Create a new class session
            ClassSession classSession = new ClassSession();
            classSession.setCourse(course);
            classSession.setProfessor(professor);
            classSession.setDate(recordRequest.getDate());
            classSession.setStartTime(recordRequest.getStartTime());
            classSession.setEndTime(recordRequest.getEndTime());
            classSession.setTopic(recordRequest.getTopic());
            
            // Save the class session
            ClassSession savedSession = classSessionRepository.save(classSession);
            
            // Generate a verification code
            String code = generateVerificationCode();
            
            // Create and save the verification code entity
            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setCode(code);
            verificationCode.setClassSession(savedSession);
            verificationCode.setExpiryTime(LocalDateTime.now().plusMinutes(15)); // Valid for 15 minutes
            
            verificationCodeRepository.save(verificationCode);
            
            // Return the class session with the verification code
            return ResponseEntity.ok(ApiResponse.success(
                    "Class recorded successfully. Verification code: " + code,
                    savedSession
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to record class: " + e.getMessage()));
        }
    }
    
    /**
     * Get all class sessions for a course (professor and admin only)
     */
    @GetMapping("/classes/course/{courseId}")
    public ResponseEntity<ApiResponse<List<ClassSession>>> getClassesByCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Get the user
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Check if user is a professor or admin
            if (user.getRole() != Role.PROFESSOR && user.getRole() != Role.ADMIN) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("Only professors and admins can access class sessions"));
            }
            
            // Get the course
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found"));
            
            // Get all class sessions for the course
            List<ClassSession> classSessions = classSessionRepository.findByCourse(course);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Class sessions retrieved successfully",
                    classSessions
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to get class sessions: " + e.getMessage()));
        }
    }
    
    /**
     * Verify attendance by code
     */
    @PostMapping("/verify-by-code")
    public ResponseEntity<ApiResponse<Attendance>> verifyAttendanceByCode(
            @RequestParam Long courseId,
            @RequestParam String verificationCode,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            // Get the student
            User student = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Get the course
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Course not found"));
            
            // Check if the student is enrolled in the course
            if (!student.getCourses().contains(course)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Student is not enrolled in this course"));
            }
            
            // Find the verification code
            Optional<VerificationCode> codeOptional = verificationCodeRepository.findByCode(verificationCode);
            
            if (codeOptional.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid verification code"));
            }
            
            VerificationCode code = codeOptional.get();
            
            // Check if the code is expired
            if (code.getExpiryTime().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Verification code has expired"));
            }
            
            // Check if the code is for the right course
            if (!code.getClassSession().getCourse().getId().equals(courseId)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Verification code is not valid for this course"));
            }
            
            // Check if student already recorded attendance for this session
            Optional<Attendance> existingAttendance = attendanceRepository
                    .findByStudentAndClassSession(student, code.getClassSession());
            
            if (existingAttendance.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Attendance already recorded for this class"));
            }
            
            // Record the attendance
            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setClassSession(code.getClassSession());
            attendance.setTimestamp(LocalDateTime.now());
            attendance.setVerificationMethod("CODE");
            attendance.setVerified(true);
            
            Attendance savedAttendance = attendanceRepository.save(attendance);
            
            return ResponseEntity.ok(ApiResponse.success(
                    "Attendance recorded successfully",
                    savedAttendance
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to verify attendance: " + e.getMessage()));
        }
    }
    
    /**
     * Generate a random 6-digit verification code
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }
}