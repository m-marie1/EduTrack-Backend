package com.college.attendance.service;

import com.college.attendance.dto.AttendanceRecordDto;
import com.college.attendance.dto.AttendanceResponseDto;
import com.college.attendance.dto.UserDto;
import com.college.attendance.exception.ResourceNotFoundException;
import com.college.attendance.model.AttendanceRecord;
import com.college.attendance.model.Course;
import com.college.attendance.model.User;
import com.college.attendance.repository.AttendanceRepository;
import com.college.attendance.repository.CourseRepository;
import com.college.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream; // Added import
import java.io.IOException; // Added import
import java.io.OutputStreamWriter; // Added import
import java.io.Writer; // Added import
import java.nio.charset.StandardCharsets; // Added import
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // Added import
import java.time.LocalDateTime;
import java.time.ZoneId; // Added import
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat; // Added import
import org.apache.commons.csv.CSVPrinter; // Added import

import lombok.extern.slf4j.Slf4j; // Added import

@Service
@Slf4j // Added for logging
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AttendanceSessionService attendanceSessionService; // Added for code verification

    @Override
    public AttendanceResponseDto recordAttendance(User user, AttendanceRecordDto attendanceDto) {
        // Get the course
        Course course = courseRepository.findById(attendanceDto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Verify the user is enrolled in this course
        if (user.getCourses() == null || !user.getCourses().contains(course)) {
            throw new IllegalStateException("User is not enrolled in this course");
        }

        // Schedule check removed as per new requirements
        LocalDateTime now = LocalDateTime.now();

        // Check if the student already has attendance for this course today
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        Optional<AttendanceRecord> existingRecord = attendanceRepository
                .findByUserAndCourseAndTimestampBetween(user, course, startOfDay, endOfDay);

        if (existingRecord.isPresent()) {
            throw new IllegalStateException("Attendance already recorded for this course today");
        }

        // Verify the submitted code against active sessions
        Optional<com.college.attendance.model.AttendanceSession> validSession = attendanceSessionService.findValidSession(
                course, attendanceDto.getVerificationCode(), now);

        if (validSession.isEmpty()) {
            throw new IllegalStateException("Invalid or expired verification code for this course.");
        }

        // If we reach here, the code is valid and the session is active/not expired.
        boolean verified = true;

        // Create and save the attendance record
        AttendanceRecord record = new AttendanceRecord();
        record.setUser(user);
        record.setCourse(course);
        record.setTimestamp(now);
        record.setVerified(verified); // Set based on code verification result

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

    private AttendanceResponseDto convertToDto(AttendanceRecord record) {
        AttendanceResponseDto dto = new AttendanceResponseDto();
        dto.setId(record.getId());
        dto.setStudentName(record.getUser().getFullName());
        dto.setStudentId(record.getUser().getStudentId() != null ? record.getUser().getStudentId() : "N/A");
        dto.setCourseCode(record.getCourse().getCourseCode());
        dto.setCourseName(record.getCourse().getCourseName());
        dto.setTimestamp(record.getTimestamp());
        dto.setVerified(record.isVerified()); // Represents code verification status
        return dto;
    }

    @Override
    public List<UserDto> getAttendeesForCourseOnDate(Long courseId, LocalDate date) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        List<AttendanceRecord> verifiedRecords = attendanceRepository
                .findByCourseAndTimestampBetweenAndVerifiedTrue(course, startOfDay, endOfDay);

        return verifiedRecords.stream()
                .map(AttendanceRecord::getUser) // Get the User from the record
                .distinct() // Ensure each user appears only once even if multiple records exist (shouldn't happen with current logic)
                .map(this::convertToUserDto) // Convert User to UserDto
                .collect(Collectors.toList());
    }

    // Helper method to convert User entity to UserDto
    private UserDto convertToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setStudentId(user.getStudentId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getCourseAttendanceSpreadsheet(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        List<AttendanceRecord> attendanceRecords = attendanceRepository.findByCourse(course);
        if (attendanceRecords.isEmpty()) {
            // Return empty byte array instead of throwing exception, controller handles 204
            return new byte[0];
        }

        // Use Apache Commons CSV
        String[] headers = {"Student Name", "Student ID", "Date", "Time", "Verified"};
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Africa/Cairo"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Ensure UTF-8 encoding for broader compatibility
        try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers))) {

            for (AttendanceRecord record : attendanceRecords) {
                // Convert timestamp to Africa/Cairo zone
                var zoned = record.getTimestamp().atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("Africa/Cairo"));
                csvPrinter.printRecord(
                        record.getUser().getFullName(),
                        record.getUser().getStudentId() != null ? record.getUser().getStudentId() : "N/A",
                        zoned.toLocalDate().format(dateFormatter),
                        zoned.toLocalTime().format(timeFormatter),
                        record.isVerified() ? "Yes" : "No"
                );
            }
            csvPrinter.flush(); // Ensure all data is written
            return out.toByteArray();

        } catch (IOException e) {
            // Log the error and rethrow as a runtime exception or return empty/error indicator
            log.error("Error generating CSV for course {}: {}", courseId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate attendance spreadsheet", e);
        }
    }
}