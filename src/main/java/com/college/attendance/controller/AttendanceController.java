package com.college.attendance.controller;

import com.college.attendance.dto.AttendanceRecordDto;
import com.college.attendance.dto.AttendanceResponseDto;
import com.college.attendance.dto.UserDto;
import com.college.attendance.exception.ResourceNotFoundException;
import com.college.attendance.model.User;
import com.college.attendance.repository.UserRepository;
import com.college.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@Slf4j
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;
    // Removed CourseRepository as it wasn't used directly here

    @PostMapping("/record")
    @PreAuthorize("hasRole('STUDENT')") // Ensure only students can record attendance this way
    public ResponseEntity<ApiResponse<AttendanceResponseDto>> recordAttendance(
            @Valid @RequestBody AttendanceRecordDto attendanceDto) {
        // Exceptions handled by RestExceptionHandler

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        AttendanceResponseDto response = attendanceService.recordAttendance(user, attendanceDto);
        return ResponseEntity.ok(ApiResponse.success("Attendance recorded successfully", response));
    }

    @PostMapping("/enroll/{courseId}")
    public ResponseEntity<ApiResponse<String>> enrollInCourse(@PathVariable Long courseId) {
        // Exceptions handled by RestExceptionHandler

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with username: " + username));

        // Optional: Add an explicit check, although @PreAuthorize should handle it
        // if (user.getRole() != com.college.attendance.model.Role.STUDENT) {
        //     throw new AccessDeniedException("Only students can record attendance.");
        // }

        String result = attendanceService.enrollUserInCourse(user, courseId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/user/{userId}/course/{courseId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getUserAttendanceForCourse(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        // Service layer handles ResourceNotFoundException if user/course not found
        List<AttendanceResponseDto> attendanceList =
                attendanceService.getUserAttendanceForCourse(userId, courseId);
        return ResponseEntity.ok(ApiResponse.success(attendanceList));
    }

    @GetMapping("/user/username/{username}/course/{courseId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getUserAttendanceForCourseByUsername(
            @PathVariable String username,
            @PathVariable Long courseId) {
        // Exceptions handled by RestExceptionHandler
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        List<AttendanceResponseDto> attendanceList =
                attendanceService.getUserAttendanceForCourse(user.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success(attendanceList));
    }

    @GetMapping("/user/current/course/{courseId}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getCurrentUserAttendanceForCourse(
            @PathVariable Long courseId) {
        // Exceptions handled by RestExceptionHandler
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        List<AttendanceResponseDto> attendanceList =
                attendanceService.getUserAttendanceForCourse(user.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success(attendanceList));
    }

    @GetMapping("/user/{userId}/date/{date}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getUserAttendanceForDate(
            @PathVariable Long userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // Service layer handles ResourceNotFoundException if user not found
        List<AttendanceResponseDto> attendanceList =
                attendanceService.getUserAttendanceForDate(userId, date);
        return ResponseEntity.ok(ApiResponse.success(attendanceList));
    }

    @GetMapping("/course/{courseId}/date/{date}")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDto>>> getCourseAttendanceForDate(
            @PathVariable Long courseId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // Service layer handles ResourceNotFoundException if course not found
        List<AttendanceResponseDto> attendanceList =
                attendanceService.getCourseAttendanceForDate(courseId, date);
        return ResponseEntity.ok(ApiResponse.success(attendanceList));
    }

    @GetMapping("/user/{userId}/course/{courseId}/present")
    public ResponseEntity<ApiResponse<Boolean>> isUserPresentToday(
            @PathVariable Long userId,
            @PathVariable Long courseId) {
        // Service layer handles ResourceNotFoundException if user/course not found
        boolean isPresent = attendanceService.isUserPresentForCourseToday(userId, courseId);
        return ResponseEntity.ok(ApiResponse.success(
                isPresent ? "Student is present today" : "Student is not present today",
                isPresent));
    }

    @GetMapping("/course/{courseId}/date/{date}/attendees")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getCourseAttendeesForDate(
            @PathVariable Long courseId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        // Exceptions handled by RestExceptionHandler
        List<UserDto> attendees = attendanceService.getAttendeesForCourseOnDate(courseId, date);
        return ResponseEntity.ok(ApiResponse.success("Attendees retrieved successfully", attendees));
    }

    @GetMapping("/course/{courseId}/spreadsheet")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<byte[]> getCourseAttendanceSpreadsheet(@PathVariable Long courseId) {
        // Exceptions handled by RestExceptionHandler
        byte[] csvBytes = attendanceService.getCourseAttendanceSpreadsheet(courseId);

        // Check if service returned empty data (e.g., no records found)
        // The service throws IllegalStateException if no records, which RestExceptionHandler handles (returning 204)
        // If the service might return an empty byte array instead of throwing, handle it here:
        if (csvBytes == null || csvBytes.length == 0) {
             return ResponseEntity.noContent().build(); // Return 204 No Content
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN); // Use TEXT_PLAIN for broader compatibility
        headers.setContentDispositionFormData("attachment",
                String.format("attendance-course-%d-%s.csv", courseId, LocalDate.now()));

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }
}