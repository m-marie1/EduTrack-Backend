package com.college.attendance.repository;

import com.college.attendance.model.Question;
import com.college.attendance.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuiz(Quiz quiz);
    
    List<Question> findByQuizOrderByOrderAsc(Quiz quiz);
} 