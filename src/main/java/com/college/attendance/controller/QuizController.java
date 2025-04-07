package com.college.attendance.controller;

import com.college.attendance.dto.QuizAttemptDto;
import com.college.attendance.dto.QuizDto;
import com.college.attendance.model.*;
import com.college.attendance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {
    
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    
    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<Quiz>> createQuiz(@Valid @RequestBody QuizDto quizDto) {
        try {
            // Get the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Get the course
            Course course = courseRepository.findById(quizDto.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
            
            // Create and save the quiz
            Quiz quiz = new Quiz();
            quiz.setTitle(quizDto.getTitle());
            quiz.setDescription(quizDto.getDescription());
            quiz.setCourse(course);
            quiz.setCreator(creator);
            quiz.setStartDate(quizDto.getStartDate());
            quiz.setEndDate(quizDto.getEndDate());
            quiz.setDurationMinutes(quizDto.getDurationMinutes());
            
            // Create questions list
            List<Question> questions = new ArrayList<>();
            
            if (quizDto.getQuestions() != null && !quizDto.getQuestions().isEmpty()) {
                for (int i = 0; i < quizDto.getQuestions().size(); i++) {
                    var questionDto = quizDto.getQuestions().get(i);
                    
                    Question question = new Question();
                    question.setQuiz(quiz);
                    question.setText(questionDto.getText());
                    question.setImageUrl(questionDto.getImageUrl());
                    question.setType(questionDto.getType());
                    question.setPoints(questionDto.getPoints() != null ? questionDto.getPoints() : 1);
                    question.setOrder(i + 1);
                    
                    // For text answer questions
                    if (questionDto.getType() == QuestionType.TEXT_ANSWER) {
                        question.setCorrectAnswer(questionDto.getCorrectAnswer());
                    }
                    
                    // For multiple choice questions, create options
                    if (questionDto.getType() == QuestionType.MULTIPLE_CHOICE && 
                        questionDto.getOptions() != null && !questionDto.getOptions().isEmpty()) {
                        
                        List<QuestionOption> options = new ArrayList<>();
                        
                        for (int j = 0; j < questionDto.getOptions().size(); j++) {
                            var optionDto = questionDto.getOptions().get(j);
                            
                            QuestionOption option = new QuestionOption();
                            option.setQuestion(question);
                            option.setText(optionDto.getText());
                            option.setCorrect(optionDto.getCorrect());
                            option.setOrder(j + 1);
                            
                            options.add(option);
                        }
                        
                        question.setOptions(options);
                    }
                    
                    questions.add(question);
                }
            }
            
            quiz.setQuestions(questions);
            
            // Save the entire graph at once
            Quiz savedQuiz = quizRepository.save(quiz);
            
            // To prevent circular reference issues, create a clean response without circular references
            Quiz responseQuiz = quizRepository.findById(savedQuiz.getId())
                .orElseThrow(() -> new IllegalArgumentException("Failed to retrieve saved quiz"));
            
            // Clear any circular references
            if (responseQuiz.getQuestions() != null) {
                responseQuiz.getQuestions().forEach(q -> q.setQuiz(null));
            }
            
            return ResponseEntity.ok(
                ApiResponse.success("Quiz created successfully", responseQuiz)
            );
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to create quiz: " + e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Quiz>>> getQuizzesByCourse(
            @RequestParam Long courseId) {
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        
        List<Quiz> quizzes = quizRepository.findByCourse(course);
        
        return ResponseEntity.ok(ApiResponse.success(quizzes));
    }
    
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<Quiz>>> getAvailableQuizzes(
            @RequestParam Long courseId) {
        
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        
        LocalDateTime now = LocalDateTime.now();
        
        List<Quiz> quizzes = quizRepository
            .findByCourseAndStartDateBeforeAndEndDateAfter(course, now, now);
        
        return ResponseEntity.ok(ApiResponse.success(quizzes));
    }
    
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<Quiz>> getQuizById(@PathVariable Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        
        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // If student, check if quiz is available
        if (user.getRole() == Role.STUDENT) {
            LocalDateTime now = LocalDateTime.now();
            
            if (now.isBefore(quiz.getStartDate()) || now.isAfter(quiz.getEndDate())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Quiz is not available at this time"));
            }
        }
        
        return ResponseEntity.ok(ApiResponse.success(quiz));
    }
    
    @PostMapping("/{quizId}/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<QuizAttempt>> startQuiz(@PathVariable Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User student = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if quiz is available
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(quiz.getStartDate()) || now.isAfter(quiz.getEndDate())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Quiz is not available at this time"));
        }
        
        // Check if student already has an active attempt
        Optional<QuizAttempt> existingAttempt = 
            quizAttemptRepository.findByQuizAndStudentAndCompleted(quiz, student, false);
        
        if (existingAttempt.isPresent()) {
            return ResponseEntity.ok(
                ApiResponse.success("Continuing existing attempt", existingAttempt.get())
            );
        }
        
        // Check if student has already completed this quiz
        List<QuizAttempt> completedAttempts = 
            quizAttemptRepository.findByQuizAndStudent(quiz, student)
                .stream()
                .filter(QuizAttempt::isCompleted)
                .collect(Collectors.toList());
        
        if (!completedAttempts.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("You have already completed this quiz"));
        }
        
        // Create a new attempt
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setStartTime(now);
        attempt.setCompleted(false);
        
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        
        return ResponseEntity.ok(
            ApiResponse.success("Quiz started successfully", savedAttempt)
        );
    }
    
    @PostMapping("/{quizId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<QuizAttempt>> submitQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizAttemptDto attemptDto) {
        
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User student = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Find the active attempt
        QuizAttempt attempt = quizAttemptRepository
            .findByQuizAndStudentAndCompleted(quiz, student, false)
            .orElseThrow(() -> new IllegalArgumentException("No active quiz attempt found"));
        
        // Check if time is up
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = attempt.getStartTime().plusMinutes(quiz.getDurationMinutes());
        
        boolean timeIsUp = now.isAfter(endTime);
        
        // Process answers
        List<QuizAnswer> answers = new ArrayList<>();
        int score = 0;
        int maxScore = 0;
        
        for (var answerDto : attemptDto.getAnswers()) {
            Question question = questionRepository.findById(answerDto.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
            
            QuizAnswer answer = new QuizAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            
            if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
                if (answerDto.getSelectedOptionId() != null) {
                    QuestionOption option = questionOptionRepository
                        .findById(answerDto.getSelectedOptionId())
                        .orElse(null);
                    
                    answer.setSelectedOption(option);
                    
                    // Auto-grade multiple choice
                    if (option != null && option.isCorrect()) {
                        answer.setPointsAwarded(question.getPoints());
                        score += question.getPoints();
                    } else {
                        answer.setPointsAwarded(0);
                    }
                    
                    answer.setGraded(true);
                }
            } else if (question.getType() == QuestionType.TEXT_ANSWER) {
                answer.setTextAnswer(answerDto.getTextAnswer());
                
                // Auto-grade text answers if they match exactly
                if (answerDto.getTextAnswer() != null && 
                    answerDto.getTextAnswer().equalsIgnoreCase(question.getCorrectAnswer())) {
                    answer.setPointsAwarded(question.getPoints());
                    score += question.getPoints();
                } else {
                    answer.setPointsAwarded(0);
                }
                
                answer.setGraded(true);
            }
            
            QuizAnswer savedAnswer = quizAnswerRepository.save(answer);
            answers.add(savedAnswer);
            
            maxScore += question.getPoints();
        }
        
        // Update the attempt
        attempt.setEndTime(now);
        attempt.setCompleted(true);
        attempt.setScore(score);
        attempt.setMaxScore(maxScore);
        attempt.setAnswers(answers);
        
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        
        return ResponseEntity.ok(
            ApiResponse.success(
                timeIsUp ? "Time's up! Quiz auto-submitted." : "Quiz submitted successfully", 
                savedAttempt
            )
        );
    }
    
    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<String>> deleteQuiz(@PathVariable Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if user is the creator
        if (!quiz.getCreator().getId().equals(user.getId())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("You can only delete quizzes you created"));
        }
        
        quizRepository.delete(quiz);
        
        return ResponseEntity.ok(
            ApiResponse.success("Quiz deleted successfully")
        );
    }
}