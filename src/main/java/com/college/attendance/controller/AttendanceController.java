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
}