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
    
    // Get the authenticated user
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    
    AttendanceResponseDto response = attendanceService.recordAttendance(user, attendanceDto);
    
    return ResponseEntity.ok(ApiResponse.success("Attendance recorded successfully", response));
}


    @PostMapping("/enroll/{courseId}")
public ResponseEntity<ApiResponse<String>> enrollInCourse(@PathVariable Long courseId) {
    // Get the authenticated user
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = authentication.getName();
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    
    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    
    if (user.getCourses() == null) {
        user.setCourses(new HashSet<>());
    }
    
    user.getCourses().add(course);
    userRepository.save(user);
    
    return ResponseEntity.ok(ApiResponse.success(
        "Successfully enrolled in " + course.getCourseName(), 
        "You are now enrolled in " + course.getCourseCode())
    );
}
    
    @GetMapping("/user/{userId}/course/{courseId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getUserAttendanceForCourse(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        
        List<AttendanceResponseDto> attendanceList = 
            attendanceService.getUserAttendanceForCourse(userId, courseId);
        
        return ResponseEntity.ok(ApiResponse.success(attendanceList));
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