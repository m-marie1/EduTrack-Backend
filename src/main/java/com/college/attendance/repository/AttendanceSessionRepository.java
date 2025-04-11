package com.college.attendance.repository;

import com.college.attendance.model.AttendanceSession;
import com.college.attendance.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    /**
     * Finds an active attendance session for a specific course using the verification code.
     * Checks if the session is active and has not expired.
     *
     * @param course The course for which the session is active.
     * @param verificationCode The verification code entered by the student.
     * @param now The current time to check against the expiry time.
     * @return An Optional containing the active AttendanceSession if found, otherwise empty.
     */
    Optional<AttendanceSession> findByCourseAndVerificationCodeAndActiveTrueAndExpiresAtAfter(
            Course course, String verificationCode, LocalDateTime now);

    /**
     * Finds all active attendance sessions for a specific course.
     *
     * @param course The course to find active sessions for.
     * @param now The current time to check against the expiry time.
     * @return A list of active attendance sessions for the course.
     */
    List<AttendanceSession> findByCourseAndActiveTrueAndExpiresAtAfter(Course course, LocalDateTime now);

    /**
     * Finds all expired but still active sessions before a certain time.
     * Useful for a cleanup task to deactivate expired sessions.
     *
     * @param now The current time.
     * @return A list of expired sessions that are still marked as active.
     */
    List<AttendanceSession> findByActiveTrueAndExpiresAtBefore(LocalDateTime now);

    /**
     * Finds all active attendance sessions created by a specific professor that have not yet expired.
     *
     * @param professor The professor who created the sessions.
     * @param now The current time to check against the expiry time.
     * @return A list of active, non-expired sessions for the professor.
     */
    List<AttendanceSession> findByProfessorAndActiveTrueAndExpiresAtAfter(com.college.attendance.model.User professor, LocalDateTime now);
}