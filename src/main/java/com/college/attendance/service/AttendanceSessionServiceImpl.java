package com.college.attendance.service;

import com.college.attendance.dto.SessionDto;
import com.college.attendance.dto.UserDto;
import com.college.attendance.exception.ResourceNotFoundException;
import com.college.attendance.model.*;
import com.college.attendance.repository.AttendanceRepository;
import com.college.attendance.repository.AttendanceSessionRepository;
import com.college.attendance.repository.CourseRepository;
import com.college.attendance.repository.UserRepository;
import com.college.attendance.repository.CourseAttendanceResetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceSessionServiceImpl implements AttendanceSessionService {

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final CourseAttendanceResetRepository courseAttendanceResetRepository;

    private static final Random RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;
    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Override
    @Transactional
    public SessionDto createAttendanceSession(User professor, Long courseId, int expiryMinutes) {
        if (professor.getRole() != Role.PROFESSOR) {
            throw new SecurityException("User does not have professor privileges.");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        if (professor.getCourses() == null || professor.getCourses().stream().noneMatch(c -> c.getId().equals(courseId))) {
            throw new SecurityException("Professor is not associated with this course.");
        }

        if (expiryMinutes <= 0) {
            throw new IllegalArgumentException("Expiry time must be positive.");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(expiryMinutes);

        String verificationCode;
        int attempts = 0;
        int maxAttempts = 10;
        do {
            verificationCode = generateRandomCode(CODE_LENGTH);
            attempts++;
            if (attempts > maxAttempts) {
                log.error("Failed to generate a unique verification code for course {} after {} attempts.", courseId, maxAttempts);
                throw new IllegalStateException("Could not generate a unique verification code. Please try again.");
            }
        } while (attendanceSessionRepository.findByCourseAndVerificationCodeAndActiveTrueAndExpiresAtAfter(course, verificationCode, now).isPresent());

        AttendanceSession session = new AttendanceSession();
        session.setCourse(course);
        session.setProfessor(professor);
        session.setVerificationCode(verificationCode);
        session.setCreatedAt(now);
        session.setExpiresAt(expiresAt);
        session.setActive(true);

        AttendanceSession savedSession = attendanceSessionRepository.save(session);
        log.info("Created attendance session ID {} for course {} by professor {}. Code: {}, Expires: {}",
                savedSession.getId(), course.getCourseCode(), professor.getUsername(), verificationCode, expiresAt);

        return convertToDto(savedSession);
    }

    @Override
    public Optional<AttendanceSession> findValidSession(Course course, String verificationCode, LocalDateTime now) {
        return attendanceSessionRepository.findByCourseAndVerificationCodeAndActiveTrueAndExpiresAtAfter(
                course, verificationCode, now);
    }

    @Override
    @Transactional
    public int deactivateExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<AttendanceSession> expiredSessions = attendanceSessionRepository.findByActiveTrueAndExpiresAtBefore(now);
        if (!expiredSessions.isEmpty()) {
            log.info("Found {} expired attendance sessions to deactivate.", expiredSessions.size());
            for (AttendanceSession session : expiredSessions) {
                session.setActive(false);
            }
            attendanceSessionRepository.saveAll(expiredSessions);
            return expiredSessions.size();
        }
        return 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionDto> getActiveSessionsForProfessor(User professor) {
        if (professor.getRole() != Role.PROFESSOR) {
            log.warn("User {} attempted to get active sessions without PROFESSOR role.", professor.getUsername());
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        List<AttendanceSession> activeSessions = attendanceSessionRepository.findByProfessorAndActiveTrueAndExpiresAtAfter(professor, now);
        return activeSessions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getSessionAttendees(Long sessionId, User professor) {
        AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found with ID: " + sessionId));

        if (!session.getProfessor().getId().equals(professor.getId())) {
            log.warn("Professor {} (ID: {}) attempted to access attendees for session {} owned by professor ID: {}",
                     professor.getUsername(), professor.getId(), sessionId, session.getProfessor().getId());
            throw new SecurityException("Professor does not have permission to view attendees for this session.");
        }

        List<AttendanceRecord> verifiedRecords = attendanceRepository
                .findByCourseAndTimestampBetweenAndVerifiedTrue(
                        session.getCourse(),
                        session.getCreatedAt(),
                        session.getExpiresAt()
                );

        return verifiedRecords.stream()
                .map(AttendanceRecord::getUser)
                .distinct()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public int getClassDaysCount(User professor, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        LocalDateTime resetTime = courseAttendanceResetRepository
                .findByProfessorAndCourse(professor, course)
                .map(CourseAttendanceReset::getResetTimestamp)
                .orElse(LocalDateTime.MIN);
        List<AttendanceSession> sessions = attendanceSessionRepository
                .findAll().stream()
                .filter(s -> s.getProfessor().getId().equals(professor.getId()) &&
                             s.getCourse().getId().equals(courseId) &&
                             s.getCreatedAt().isAfter(resetTime))
                .toList();
        HashSet<LocalDate> uniqueDays = new HashSet<>();
        for (AttendanceSession session : sessions) {
            uniqueDays.add(session.getCreatedAt().toLocalDate());
        }
        return uniqueDays.size();
    }

    @Override
    public void resetClassDaysCount(User professor, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        CourseAttendanceReset reset = courseAttendanceResetRepository
                .findByProfessorAndCourse(professor, course)
                .orElse(new CourseAttendanceReset(null, professor, course, null));
        reset.setResetTimestamp(LocalDateTime.now());
        courseAttendanceResetRepository.save(reset);
    }

    private String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(CODE_CHARACTERS.charAt(RANDOM.nextInt(CODE_CHARACTERS.length())));
        }
        return code.toString();
    }

    private SessionDto convertToDto(AttendanceSession session) {
        return new SessionDto(
                session.getId(),
                session.getCourse().getId(),
                session.getCourse().getCourseCode(),
                session.getCourse().getCourseName(),
                session.getVerificationCode(),
                session.getCreatedAt(),
                session.getExpiresAt(),
                session.isActive()
        );
    }

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
}