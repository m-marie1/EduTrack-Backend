package com.college.attendance.repository;

import com.college.attendance.model.ClassSession;
import com.college.attendance.model.Course;
import com.college.attendance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {
    
    /**
     * Find class sessions for a specific course
     */
    List<ClassSession> findByCourse(Course course);
    
    /**
     * Find class sessions conducted by a specific professor
     */
    List<ClassSession> findByProfessor(User professor);
    
    /**
     * Find class sessions for a specific course on a specific date
     */
    List<ClassSession> findByCourseAndDate(Course course, LocalDate date);
    
    /**
     * Find class sessions for a specific professor on a specific date
     */
    List<ClassSession> findByProfessorAndDate(User professor, LocalDate date);
    
    /**
     * Find class sessions for a specific course between two dates
     */
    List<ClassSession> findByCourseAndDateBetween(Course course, LocalDate startDate, LocalDate endDate);
} 