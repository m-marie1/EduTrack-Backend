package com.college.attendance.repository;

import com.college.attendance.model.Quiz;
import com.college.attendance.model.QuizAttempt;
import com.college.attendance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByQuiz(Quiz quiz);
    
    List<QuizAttempt> findByStudent(User student);
    
    List<QuizAttempt> findByStudentAndCompleted(User student, boolean completed);
    
    List<QuizAttempt> findByQuizAndStudent(Quiz quiz, User student);
    
    Optional<QuizAttempt> findByQuizAndStudentAndCompleted(Quiz quiz, User student, boolean completed);
} 