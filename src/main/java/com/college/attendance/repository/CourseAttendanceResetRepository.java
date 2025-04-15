package com.college.attendance.repository;

import com.college.attendance.model.CourseAttendanceReset;
import com.college.attendance.model.Course;
import com.college.attendance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseAttendanceResetRepository extends JpaRepository<CourseAttendanceReset, Long> {
    Optional<CourseAttendanceReset> findByProfessorAndCourse(User professor, Course course);
}
