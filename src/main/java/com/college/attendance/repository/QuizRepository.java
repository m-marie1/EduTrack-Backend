package com.college.attendance.repository;

import com.college.attendance.model.Course;
import com.college.attendance.model.Quiz;
import com.college.attendance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCourse(Course course);
    
    List<Quiz> findByCreator(User creator);
    
    List<Quiz> findByCourseAndEndDateAfter(Course course, LocalDateTime now);
    
    List<Quiz> findByCourseAndStartDateBeforeAndEndDateAfter(
        Course course, LocalDateTime now, LocalDateTime now2);
} 