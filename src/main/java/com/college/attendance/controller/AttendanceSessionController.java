package com.college.attendance.controller;

import com.college.attendance.dto.CreateSessionRequestDto;
import com.college.attendance.dto.SessionDto;
import com.college.attendance.exception.ResourceNotFoundException;
import com.college.attendance.model.User;
import com.college.attendance.repository.UserRepository;
import com.college.attendance.service.AttendanceSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*; // Use wildcard for common annotations
import java.util.List; // Added import
import com.college.attendance.dto.UserDto; // Added import

@RestController
@RequestMapping("/api/attendance/sessions")
@RequiredArgsConstructor
@Slf4j
public class AttendanceSessionController {

    private final AttendanceSessionService attendanceSessionService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    @PreAuthorize("hasRole('PROFESSOR')") // Only professors can create sessions
    public ResponseEntity<ApiResponse<SessionDto>> createAttendanceSession(
            @Valid @RequestBody CreateSessionRequestDto createSessionRequest) {
        // Exceptions handled by RestExceptionHandler

        // Get the authenticated professor
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found with username: " + username));

        // Call the service to create the session
        SessionDto sessionDto = attendanceSessionService.createAttendanceSession(
                professor,
                createSessionRequest.getCourseId(),
                createSessionRequest.getExpiryMinutes()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance session created successfully", sessionDto));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<List<SessionDto>>> getActiveSessions() {
        // Exceptions handled by RestExceptionHandler
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found with username: " + username));

        List<SessionDto> activeSessions = attendanceSessionService.getActiveSessionsForProfessor(professor);
        return ResponseEntity.ok(ApiResponse.success("Active sessions retrieved successfully", activeSessions));
    }

    @GetMapping("/{sessionId}/attendees")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getSessionAttendees(@PathVariable Long sessionId) {
        // Exceptions handled by RestExceptionHandler
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found with username: " + username));

        List<UserDto> attendees = attendanceSessionService.getSessionAttendees(sessionId, professor);
        return ResponseEntity.ok(ApiResponse.success("Session attendees retrieved successfully", attendees));
    }

    @GetMapping("/class-days-count/{courseId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<Integer>> getClassDaysCount(@PathVariable Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found with username: " + username));
        int count = attendanceSessionService.getClassDaysCount(professor, courseId);
        return ResponseEntity.ok(ApiResponse.success("Class days count retrieved successfully", count));
    }

    @PostMapping("/class-days-count/{courseId}/reset")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<Void>> resetClassDaysCount(@PathVariable Long courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Professor not found with username: " + username));
        attendanceSessionService.resetClassDaysCount(professor, courseId);
        return ResponseEntity.ok(ApiResponse.success("Class days count reset successfully", null));
    }
}