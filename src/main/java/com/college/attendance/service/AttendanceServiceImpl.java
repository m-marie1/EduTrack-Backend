package com.college.attendance.service;

import com.college.attendance.dto.AttendanceRecordDto;
import com.college.attendance.dto.AttendanceResponseDto;
import com.college.attendance.exception.ResourceNotFoundException;
import com.college.attendance.model.AttendanceRecord;
import com.college.attendance.model.Course;
import com.college.attendance.model.User;
import com.college.attendance.repository.AttendanceRepository;
import com.college.attendance.repository.CourseRepository;
import com.college.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final NetworkVerificationService networkVerificationService;
    
    @Override
    public AttendanceResponseDto recordAttendance(User user, AttendanceRecordDto attendanceDto) {
        // Get the course
        Course course = courseRepository.findById(attendanceDto.getCourseId())
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        // Verify the user is enrolled in this course
        if (user.getCourses() == null || !user.getCourses().contains(course)) {
            throw new IllegalArgumentException("User is not enrolled in this course");
        }
        
        // Check if the current time is within the course schedule for today
        LocalDateTime now = LocalDateTime.now();
        if (!isCourseInSession(course, now)) {
            throw new IllegalArgumentException("Course is not in session right now");
        }
        
        // Check if the student already has attendance for this course today
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        
        Optional<AttendanceRecord> existingRecord = attendanceRepository
            .findByUserAndCourseAndTimestampBetween(user, course, startOfDay, endOfDay);
        
        if (existingRecord.isPresent()) {
            throw new IllegalArgumentException("Attendance already recorded for this course today");
        }
        
        // Verify network connection (wifi or ESP32)
        boolean verified = networkVerificationService.verifyNetworkConnection(
            attendanceDto.getNetworkIdentifier(), 
            attendanceDto.getVerificationMethod()
        );
        
        // Create and save the attendance record
        AttendanceRecord record = new AttendanceRecord();
        record.setUser(user);
        record.setCourse(course);
        record.setTimestamp(now);
        record.setVerified(verified);
        record.setNetworkIdentifier(attendanceDto.getNetworkIdentifier());
        record.setVerificationMethod(attendanceDto.getVerificationMethod());
        
        AttendanceRecord savedRecord = attendanceRepository.save(record);
        
        // Convert to DTO and return
        return convertToDto(savedRecord);
    }
    
    @Override
    public List<AttendanceResponseDto> getUserAttendanceForCourse(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            
        List<AttendanceRecord> records = attendanceRepository.findByUserAndCourse(user, course);
        
        return records.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<AttendanceResponseDto> getUserAttendanceForDate(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        
        List<AttendanceRecord> records = attendanceRepository
            .findByUserAndTimestampBetween(user, startOfDay, endOfDay);
            
        return records.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<AttendanceResponseDto> getCourseAttendanceForDate(Long courseId, LocalDate date) {
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        
        List<AttendanceRecord> records = attendanceRepository
            .findByCourseAndTimestampBetween(course, startOfDay, endOfDay);
            
        return records.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean isUserPresentForCourseToday(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);
        
        return attendanceRepository
            .findByUserAndCourseAndTimestampBetween(user, course, startOfDay, endOfDay)
            .isPresent();
    }
    
    @Override
    public String enrollUserInCourse(User user, Long courseId) {
        // Find the course
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        
        // Check if user already has courses
        if (user.getCourses() == null) {
            user.setCourses(new java.util.HashSet<>());
        }
        
        // Check if already enrolled
        if (user.getCourses().stream().anyMatch(c -> c.getId().equals(courseId))) {
            return "Already enrolled in " + course.getCourseCode();
        }
        
        // Add course to user and save
        user.getCourses().add(course);
        userRepository.save(user);
        
        return "Successfully enrolled in " + course.getCourseCode();
    }
    
    private boolean isCourseInSession(Course course, LocalDateTime now) {
        return course.getDays().contains(now.getDayOfWeek()) &&
               !now.toLocalTime().isBefore(course.getStartTime()) &&
               !now.toLocalTime().isAfter(course.getEndTime());
    }
    
    private AttendanceResponseDto convertToDto(AttendanceRecord record) {
        AttendanceResponseDto dto = new AttendanceResponseDto();
        dto.setId(record.getId());
        dto.setStudentName(record.getUser().getFullName());
        dto.setStudentId(record.getUser().getStudentId());
        dto.setCourseCode(record.getCourse().getCourseCode());
        dto.setCourseName(record.getCourse().getCourseName());
        dto.setTimestamp(record.getTimestamp());
        dto.setVerified(record.isVerified());
        dto.setVerificationMethod(record.getVerificationMethod());
        return dto;
    }
}