package com.college.attendance.repository;

import com.college.attendance.model.AttendanceRecord;
import com.college.attendance.model.Course;
import com.college.attendance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByUserAndCourse(User user, Course course);
    
    List<AttendanceRecord> findByUserAndTimestampBetween(
        User user, LocalDateTime start, LocalDateTime end);
    
    List<AttendanceRecord> findByCourseAndTimestampBetween(
        Course course, LocalDateTime start, LocalDateTime end);
    
    Optional<AttendanceRecord> findByUserAndCourseAndTimestampBetween(
        User user, Course course, LocalDateTime start, LocalDateTime end);

    List<AttendanceRecord> findByCourseAndTimestampBetweenAndVerifiedTrue(
            Course course, LocalDateTime start, LocalDateTime end);

    List<AttendanceRecord> findByCourse(Course course);
}