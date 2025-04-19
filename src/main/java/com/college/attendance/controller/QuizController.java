package com.college.attendance.controller;

import com.college.attendance.dto.QuizAttemptDto;
import com.college.attendance.dto.QuizDto;
import com.college.attendance.model.*;
import com.college.attendance.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        try {
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
            
            LocalDateTime now = LocalDateTime.now();
            System.out.println("Current time: " + now);
            
            // First get all quizzes for the course
            List<Quiz> allQuizzes = quizRepository.findByCourse(course);
            System.out.println("Total quizzes for course: " + allQuizzes.size());
            
            // Log details about each quiz
            for (Quiz quiz : allQuizzes) {
                System.out.println("Quiz ID: " + quiz.getId() + 
                                   ", Title: " + quiz.getTitle() + 
                                   ", Start Date: " + quiz.getStartDate() + 
                                   ", End Date: " + quiz.getEndDate());
            }
            
            // Get available quizzes using the improved repository method
            List<Quiz> availableQuizzes = quizRepository.findAvailableQuizzesByCourse(course, now);
            
            System.out.println("Available quizzes: " + availableQuizzes.size());
            
            return ResponseEntity.ok(ApiResponse.success(availableQuizzes));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Error retrieving available quizzes: " + e.getMessage()));
        }
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
    
    @PutMapping("/{quizId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<Quiz>> updateQuiz(
            @PathVariable Long quizId, 
            @Valid @RequestBody QuizDto quizDto) {
        try {
            // Get the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User professor = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
            // Get the quiz
            Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
                
            // Check if the professor created this quiz
            if (!quiz.getCreator().getId().equals(professor.getId())) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("You can only update quizzes you created"));
            }
            
            // Check if quiz has any attempts - if so, restrict certain changes
            boolean hasAttempts = !quizAttemptRepository.findByQuiz(quiz).isEmpty();
            
            // Update basic quiz details
            quiz.setTitle(quizDto.getTitle());
            quiz.setDescription(quizDto.getDescription());
            
            // Only allow changing dates/duration if no attempts yet
            if (!hasAttempts) {
                quiz.setStartDate(quizDto.getStartDate());
                quiz.setEndDate(quizDto.getEndDate());
                quiz.setDurationMinutes(quizDto.getDurationMinutes());
                
                // Handle questions update
                if (quizDto.getQuestions() != null) {
                    // Delete existing questions
                    List<Question> existingQuestions = quiz.getQuestions();
                    if (existingQuestions != null) {
                        questionRepository.deleteAll(existingQuestions);
                    }
                    
                    // Create new questions list
                    List<Question> newQuestions = new ArrayList<>();
                    
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
                        
                        newQuestions.add(question);
                    }
                    
                    quiz.setQuestions(newQuestions);
                }
            } else {
                // Just update dates if there are already attempts
                quiz.setEndDate(quizDto.getEndDate()); // Allow extending the end date
            }
            
            // Save the entire graph at once
            Quiz updatedQuiz = quizRepository.save(quiz);
            
            // Clear any circular references
            if (updatedQuiz.getQuestions() != null) {
                updatedQuiz.getQuestions().forEach(q -> q.setQuiz(null));
            }
            
            return ResponseEntity.ok(
                hasAttempts
                    ? ApiResponse.success("Quiz updated with limited changes due to existing attempts", updatedQuiz)
                    : ApiResponse.success("Quiz updated successfully", updatedQuiz)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to update quiz: " + e.getMessage()));
        }
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
    public ResponseEntity<?> submitQuiz(
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
        
        // Update the attempt WITHOUT setting the answers collection
        // to avoid orphan deletion cascading
        attempt.setEndTime(now);
        attempt.setCompleted(true);
        attempt.setScore(score);
        attempt.setMaxScore(maxScore);
        
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);
        
        // To avoid circular reference issues in the response and Hibernate orphan deletion,
        // we'll work with a detached copy of the quiz attempt
        QuizAttempt savedAttemptWithId = savedAttempt;
        
        // Create a simplified response object to avoid circular references
        Map<String, Object> simplifiedResponse = new HashMap<>();
        simplifiedResponse.put("id", savedAttemptWithId.getId());
        simplifiedResponse.put("quiz", savedAttemptWithId.getQuiz() != null ? savedAttemptWithId.getQuiz().getId() : null);
        simplifiedResponse.put("student", savedAttemptWithId.getStudent() != null ? savedAttemptWithId.getStudent().getId() : null);
        simplifiedResponse.put("startTime", savedAttemptWithId.getStartTime());
        simplifiedResponse.put("endTime", savedAttemptWithId.getEndTime());
        simplifiedResponse.put("completed", savedAttemptWithId.isCompleted());
        simplifiedResponse.put("score", savedAttemptWithId.getScore());
        simplifiedResponse.put("maxScore", savedAttemptWithId.getMaxScore());
        
        // Manually load answer data to avoid Hibernate orphan issues
        List<Map<String, Object>> simplifiedAnswers = new ArrayList<>();
        List<QuizAnswer> answerList = quizAnswerRepository.findByAttemptId(savedAttemptWithId.getId());
            
        for (QuizAnswer answer : answerList) {
            Map<String, Object> simplifiedAnswer = new HashMap<>();
            simplifiedAnswer.put("id", answer.getId());
            simplifiedAnswer.put("question", answer.getQuestion() != null ? answer.getQuestion().getId() : null);
            simplifiedAnswer.put("selectedOption", answer.getSelectedOption() != null ? answer.getSelectedOption().getId() : null);
            simplifiedAnswer.put("textAnswer", answer.getTextAnswer());
            simplifiedAnswer.put("pointsAwarded", answer.getPointsAwarded());
            simplifiedAnswer.put("graded", answer.isGraded());
            
            simplifiedAnswers.add(simplifiedAnswer);
        }
        
        simplifiedResponse.put("answers", simplifiedAnswers);
        
        return ResponseEntity.ok(
            ApiResponse.success(
                timeIsUp ? "Time's up! Quiz auto-submitted." : "Quiz submitted successfully", 
                simplifiedResponse
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
    
    @GetMapping("/{quizId}/submissions")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getQuizSubmissions(@PathVariable Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if user is the creator
        if (!quiz.getCreator().getId().equals(professor.getId())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("You can only view submissions for quizzes you created"));
        }
        
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizAndCompleted(quiz, true);
        List<Map<String, Object>> submissions = new ArrayList<>();
        
        for (QuizAttempt attempt : attempts) {
            Map<String, Object> submission = new HashMap<>();
            submission.put("id", attempt.getId());
            submission.put("studentId", attempt.getStudent().getId());
            submission.put("studentName", attempt.getStudent().getFullName());
            submission.put("startTime", attempt.getStartTime());
            submission.put("endTime", attempt.getEndTime());
            submission.put("score", attempt.getScore());
            submission.put("maxScore", attempt.getMaxScore());
            submissions.add(submission);
        }
        
        return ResponseEntity.ok(ApiResponse.success(submissions));
    }
    
    @GetMapping("/{quizId}/submissions/{submissionId}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSubmissionDetails(
            @PathVariable Long quizId,
            @PathVariable Long submissionId) {
        
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if user is the creator
        if (!quiz.getCreator().getId().equals(professor.getId())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("You can only view submissions for quizzes you created"));
        }
        
        QuizAttempt attempt = quizAttemptRepository.findById(submissionId)
            .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        
        // Verify this submission belongs to the specified quiz
        if (!attempt.getQuiz().getId().equals(quizId)) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Submission does not belong to this quiz"));
        }
        
        Map<String, Object> submissionDetails = new HashMap<>();
        submissionDetails.put("id", attempt.getId());
        submissionDetails.put("student", mapStudentInfo(attempt.getStudent()));
        submissionDetails.put("startTime", attempt.getStartTime());
        submissionDetails.put("endTime", attempt.getEndTime());
        submissionDetails.put("score", attempt.getScore());
        submissionDetails.put("maxScore", attempt.getMaxScore());
        
        // Get detailed answers
        List<Map<String, Object>> answers = new ArrayList<>();
        List<QuizAnswer> quizAnswers = quizAnswerRepository.findByAttempt(attempt);
        
        for (QuizAnswer answer : quizAnswers) {
            Map<String, Object> answerDetails = new HashMap<>();
            Question question = answer.getQuestion();
            
            answerDetails.put("questionId", question.getId());
            answerDetails.put("questionText", question.getText());
            answerDetails.put("questionType", question.getType());
            answerDetails.put("points", question.getPoints());
            answerDetails.put("pointsAwarded", answer.getPointsAwarded());
            
            if (question.getType() == QuestionType.MULTIPLE_CHOICE) {
                answerDetails.put("selectedOption", answer.getSelectedOption() != null ?
                    mapOption(answer.getSelectedOption()) : null);
                answerDetails.put("correctOption", question.getOptions().stream()
                    .filter(QuestionOption::isCorrect)
                    .findFirst()
                    .map(this::mapOption)
                    .orElse(null));
            } else if (question.getType() == QuestionType.TEXT_ANSWER) {
                answerDetails.put("studentAnswer", answer.getTextAnswer());
                answerDetails.put("correctAnswer", question.getCorrectAnswer());
            }
            
            answers.add(answerDetails);
        }
        
        submissionDetails.put("answers", answers);
        
        return ResponseEntity.ok(ApiResponse.success(submissionDetails));
    }
    
    @GetMapping("/my-quizzes")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ApiResponse<List<Quiz>>> getMyQuizzes() {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<Quiz> quizzes = quizRepository.findByCreator(professor);
        return ResponseEntity.ok(ApiResponse.success(quizzes));
    }

    @GetMapping("/{quizId}/submissions/download")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Resource> downloadSubmissions(@PathVariable Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User professor = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if user is the creator
        if (!quiz.getCreator().getId().equals(professor.getId())) {
            throw new IllegalArgumentException("You can only download submissions for quizzes you created");
        }
        
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizAndCompleted(quiz, true);
        
        // Create CSV content
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("Student ID,Student Name,Email,Submission Date,Score,Max Score,Percentage\n");
        
        for (QuizAttempt attempt : attempts) {
            User student = attempt.getStudent();
            csvContent.append(String.format("%s,%s,%s,%s,%d,%d,%.2f%%\n",
                student.getStudentId(),
                student.getFullName().replace(",", ";"), // Escape commas in names
                student.getEmail(),
                attempt.getEndTime(),
                attempt.getScore(),
                attempt.getMaxScore(),
                (attempt.getScore() * 100.0 / attempt.getMaxScore())
            ));
        }
        
        byte[] csvBytes = csvContent.toString().getBytes();
        ByteArrayResource resource = new ByteArrayResource(csvBytes);
        
        String filename = quiz.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_submissions.csv";
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("text/csv"))
            .contentLength(csvBytes.length)
            .body(resource);
    }
    
    private Map<String, Object> mapStudentInfo(User student) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", student.getId());
        info.put("fullName", student.getFullName());
        info.put("email", student.getEmail());
        return info;
    }
    
    private Map<String, Object> mapOption(QuestionOption option) {
        Map<String, Object> mapped = new HashMap<>();
        mapped.put("id", option.getId());
        mapped.put("text", option.getText());
        mapped.put("correct", option.isCorrect());
        return mapped;
    }
}