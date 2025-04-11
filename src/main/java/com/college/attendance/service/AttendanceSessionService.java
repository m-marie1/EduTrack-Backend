package com.college.attendance.service;

import com.college.attendance.dto.SessionDto; // We'll create this DTO next
import com.college.attendance.model.AttendanceSession;
import com.college.attendance.model.Course;
import com.college.attendance.model.User;

import java.time.LocalDateTime;
import java.util.List; // Added import
import com.college.attendance.dto.UserDto; // Added import
import java.util.Optional;

public interface AttendanceSessionService {

    /**
     * Creates a new attendance session for a given course, initiated by a professor.
     * Generates a unique verification code and sets the expiry time.
     *
     * @param professor The professor initiating the session.
     * @param courseId The ID of the course for the session.
     * @param expiryMinutes The duration in minutes until the session code expires.
     * @return A DTO containing the details of the created session (including the code).
     * @throws SecurityException if the user is not a professor or not enrolled in the course.
     * @throws IllegalArgumentException if expiryMinutes is invalid.
     */
    SessionDto createAttendanceSession(User professor, Long courseId, int expiryMinutes);

    /**
     * Finds an active and valid attendance session for a given course and verification code.
     *
     * @param course The course entity.
     * @param verificationCode The code provided by the student.
     * @param now The current time to check against expiry.
     * @return An Optional containing the valid AttendanceSession if found.
     */
    Optional<AttendanceSession> findValidSession(Course course, String verificationCode, LocalDateTime now);

    /**
     * Deactivates expired attendance sessions.
     * Intended to be called periodically (e.g., by a scheduled task).
     * @return The number of sessions deactivated.
     */
    int deactivateExpiredSessions();

    /**
     * Retrieves a list of currently active (not expired) attendance sessions created by a specific professor.
     *
     * @param professor The professor user.
     * @return A list of SessionDto representing the active sessions.
     */
    List<SessionDto> getActiveSessionsForProfessor(User professor);

    /**
     * Retrieves the list of students who attended a specific attendance session.
     * Verifies that the requesting professor is the one who created the session.
     *
     * @param sessionId The ID of the attendance session.
     * @param professor The professor requesting the attendee list.
     * @return A list of UserDto representing the attendees.
     * @throws ResourceNotFoundException if the session doesn't exist.
     * @throws SecurityException if the professor did not create the session.
     */
    List<UserDto> getSessionAttendees(Long sessionId, User professor);
}