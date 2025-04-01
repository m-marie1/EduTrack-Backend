package com.college.attendance.service;

import com.college.attendance.config.TestConfig;
import com.college.attendance.dto.AttendanceRecordDto;
import com.college.attendance.dto.AttendanceResponseDto;
import com.college.attendance.exception.ResourceNotFoundException;
import com.college.attendance.model.AttendanceRecord;
import com.college.attendance.model.Course;
import com.college.attendance.model.Role;
import com.college.attendance.model.User;
import com.college.attendance.repository.AttendanceRepository;
import com.college.attendance.repository.CourseRepository;
import com.college.attendance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(TestConfig.class)
public class AttendanceServiceTests {

    @MockBean
    private AttendanceRepository attendanceRepository;

    @MockBean
    private CourseRepository courseRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private NetworkVerificationService networkVerificationService;

    @Autowired
    private AttendanceService attendanceService;

    @Captor
    private ArgumentCaptor<AttendanceRecord> attendanceRecordCaptor;

    private User testUser;
    private Course testCourse;
    private AttendanceRecordDto recordDto;
    private AttendanceRecord testRecord;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        // Optional studentId for legacy compatibility
        testUser.setStudentId("12345");
        testUser.setRole(Role.STUDENT);
        testUser.setEmailVerified(true);

        // Setup test course
        testCourse = new Course();
        testCourse.setId(1L);
        testCourse.setCourseCode("CS101");
        testCourse.setCourseName("Introduction to Computer Science");
        testCourse.setDescription("Basic concepts of programming");
        testCourse.setStartTime(LocalTime.of(9, 0));
        testCourse.setEndTime(LocalTime.of(10, 30));
        testCourse.setDays(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));

        // Enroll user in course
        Set<Course> courses = new HashSet<>();
        courses.add(testCourse);
        testUser.setCourses(courses);

        // Setup attendance record DTO
        recordDto = new AttendanceRecordDto();
        recordDto.setCourseId(1L);
        recordDto.setNetworkIdentifier("College-WiFi");
        recordDto.setVerificationMethod("WIFI");

        // Setup test attendance record
        testRecord = new AttendanceRecord();
        testRecord.setId(1L);
        testRecord.setUser(testUser);
        testRecord.setCourse(testCourse);
        testRecord.setTimestamp(LocalDateTime.now());
        testRecord.setVerified(true);
        testRecord.setNetworkIdentifier("College-WiFi");
        testRecord.setVerificationMethod("WIFI");

        // Setup mocks
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(attendanceRepository.save(any(AttendanceRecord.class))).thenReturn(testRecord);
        when(networkVerificationService.verifyNetworkConnection(anyString(), anyString())).thenReturn(true);
    }

    @Test
    void testRecordAttendance_Success() {
        // Setup
        when(attendanceRepository.findByUserAndCourseAndTimestampBetween(any(), any(), any(), any()))
            .thenReturn(Optional.empty());
        
        // Test
        AttendanceResponseDto responseDto = attendanceService.recordAttendance(testUser, recordDto);
        
        // Verify
        assertNotNull(responseDto);
        assertEquals(testUser.getFullName(), responseDto.getStudentName());
        assertEquals(testUser.getStudentId(), responseDto.getStudentId());
        assertEquals(testCourse.getCourseCode(), responseDto.getCourseCode());
        assertEquals(testCourse.getCourseName(), responseDto.getCourseName());
        assertTrue(responseDto.isVerified());
        
        verify(attendanceRepository).save(attendanceRecordCaptor.capture());
        AttendanceRecord savedRecord = attendanceRecordCaptor.getValue();
        assertEquals(testUser, savedRecord.getUser());
        assertEquals(testCourse, savedRecord.getCourse());
        assertEquals("College-WiFi", savedRecord.getNetworkIdentifier());
        assertEquals("WIFI", savedRecord.getVerificationMethod());
    }

    @Test
    void testRecordAttendance_UserNotEnrolled() {
        // Setup - empty set of courses
        testUser.setCourses(new HashSet<>());
        
        // Test and verify
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.recordAttendance(testUser, recordDto);
        });
        
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void testRecordAttendance_CourseNotInSession() {
        // Setup - mock the course not in session
        // This would need to mock a method like isCourseInSession() to return false
        
        // Test and verify
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.recordAttendance(testUser, recordDto);
        });
        
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void testRecordAttendance_AlreadyRecorded() {
        // Setup
        when(attendanceRepository.findByUserAndCourseAndTimestampBetween(any(), any(), any(), any()))
            .thenReturn(Optional.of(testRecord));
        
        // Test and verify
        assertThrows(IllegalStateException.class, () -> {
            attendanceService.recordAttendance(testUser, recordDto);
        });
        
        verify(attendanceRepository, never()).save(any());
    }

    @Test
    void testEnrollUserInCourse_Success() {
        // Setup
        testUser.setCourses(new HashSet<>()); // Start with no courses
        
        // Test
        String result = attendanceService.enrollUserInCourse(testUser, 1L);
        
        // Verify
        assertTrue(testUser.getCourses().contains(testCourse));
        verify(userRepository).save(testUser);
    }

    @Test
    void testEnrollUserInCourse_CourseNotFound() {
        // Setup
        when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // Test and verify
        assertThrows(ResourceNotFoundException.class, () -> {
            attendanceService.enrollUserInCourse(testUser, 999L);
        });
    }

    @Test
    void testEnrollUserInCourse_AlreadyEnrolled() {
        // Setup - user already has the course
        
        // Test
        String result = attendanceService.enrollUserInCourse(testUser, 1L);
        
        // Verify the result indicates already enrolled
        assertTrue(result.contains("already enrolled"));
        verify(userRepository, never()).save(any()); // Should not save anything
    }
} 