package com.college.attendance.repository;

import com.college.attendance.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    
    List<Course> findByDaysContainingAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
        DayOfWeek day, LocalTime currentTime, LocalTime currentTime1);
}