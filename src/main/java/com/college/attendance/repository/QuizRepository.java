package com.college.attendance.repository;

import com.college.attendance.model.Course;
import com.college.attendance.model.Quiz;
import com.college.attendance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    
    /**
     * Find all currently available quizzes for a course.
     * Available quizzes are those that have started (startDate <= now)
     * and have not ended yet (endDate > now).
     *
     * @param course The course to find quizzes for
     * @param currentTime The current time
     * @return List of available quizzes
     */
    default List<Quiz> findAvailableQuizzesByCourse(Course course, LocalDateTime currentTime) {
        return findByCourseAndStartDateBeforeAndEndDateAfter(course, currentTime, currentTime);
    }

    List<Quiz> findByCourseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        Course course, LocalDateTime currentTime, LocalDateTime currentTimeEnd);
    
    @Query("SELECT q FROM Quiz q JOIN q.attempts a WHERE a.student.id = :studentId GROUP BY q")
    List<Quiz> findAllByStudentAttempts(Long studentId);
    
    List<Quiz> findByCreatorAndIsDraftTrue(User creator);
} 