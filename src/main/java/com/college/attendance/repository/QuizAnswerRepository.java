package com.college.attendance.repository;

import com.college.attendance.model.Question;
import com.college.attendance.model.QuizAnswer;
import com.college.attendance.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    List<QuizAnswer> findByAttempt(QuizAttempt attempt);
    
    List<QuizAnswer> findByAttemptId(Long attemptId);
    
    Optional<QuizAnswer> findByAttemptAndQuestion(QuizAttempt attempt, Question question);
} 