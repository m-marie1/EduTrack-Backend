package com.college.attendance.repository;

import com.college.attendance.model.Assignment;
import com.college.attendance.model.Course;
import com.college.attendance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourse(Course course);
    
    List<Assignment> findByCreator(User creator);
    
    List<Assignment> findByCourseAndDueDateAfter(Course course, LocalDateTime now);
    
    List<Assignment> findByCourseAndDueDateBefore(Course course, LocalDateTime now);
} 