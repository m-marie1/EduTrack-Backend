package com.college.attendance.repository;

import com.college.attendance.model.Question;
import com.college.attendance.model.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, Long> {
    List<QuestionOption> findByQuestion(Question question);
    
    List<QuestionOption> findByQuestionAndCorrect(Question question, boolean correct);
} 