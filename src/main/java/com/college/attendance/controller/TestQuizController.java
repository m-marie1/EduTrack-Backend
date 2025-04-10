package com.college.attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.college.attendance.model.Course;
import com.college.attendance.model.Quiz;
import com.college.attendance.model.User;
import com.college.attendance.repository.CourseRepository;
import com.college.attendance.repository.QuizRepository;
import com.college.attendance.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Test controller to diagnose quiz availability issues
 */
@RestController
@RequestMapping("/api/test/quizzes")
public class TestQuizController {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Test endpoint to check quiz availability logic
     */
    @GetMapping("/test-availability")
    public String testQuizAvailability() {
        StringBuilder result = new StringBuilder();
        
        // Find a course to test with
        Optional<Course> courseOpt = courseRepository.findAll().stream().findFirst();
        if (courseOpt.isEmpty()) {
            return "No courses found to test with.";
        }
        Course course = courseOpt.get();
        result.append("Testing with course: " + course.getCourseName() + " (ID: " + course.getId() + ")\n\n");
        
        // Find a user to be the creator
        Optional<User> userOpt = userRepository.findAll().stream().findFirst();
        if (userOpt.isEmpty()) {
            return "No users found to test with.";
        }
        User creator = userOpt.get();
        
        // Get current time
        LocalDateTime now = LocalDateTime.now();
        result.append("Current time: " + now + "\n\n");
        
        // Create test quizzes with different date configurations
        createTestQuiz(course, creator, "Past Quiz", 
                now.minusDays(10), now.minusDays(5), 60);
        
        createTestQuiz(course, creator, "Current Quiz", 
                now.minusDays(2), now.plusDays(5), 60);
        
        createTestQuiz(course, creator, "Future Quiz", 
                now.plusDays(2), now.plusDays(10), 60);
        
        // Get all quizzes for the course
        List<Quiz> allQuizzes = quizRepository.findByCourse(course);
        result.append("All quizzes for the course:\n");
        allQuizzes.forEach(quiz -> {
            result.append("- " + quiz.getTitle() + 
                    " (ID: " + quiz.getId() + 
                    ", Start: " + quiz.getStartDate() + 
                    ", End: " + quiz.getEndDate() + ")\n");
        });
        result.append("\n");
        
        // Get available quizzes using the improved repository method
        List<Quiz> availableQuizzes = quizRepository.findAvailableQuizzesByCourse(course, now);
        
        result.append("Available quizzes (should include only Current Quiz):\n");
        availableQuizzes.forEach(quiz -> {
            result.append("- " + quiz.getTitle() + 
                    " (ID: " + quiz.getId() + 
                    ", Start: " + quiz.getStartDate() + 
                    ", End: " + quiz.getEndDate() + ")\n");
        });
        
        return result.toString();
    }
    
    /**
     * Helper method to create a test quiz
     */
    private Quiz createTestQuiz(Course course, User creator, String title, 
                               LocalDateTime startDate, LocalDateTime endDate, 
                               Integer durationMinutes) {
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setDescription("Test quiz for availability debugging");
        quiz.setCourse(course);
        quiz.setCreator(creator);
        quiz.setStartDate(startDate);
        quiz.setEndDate(endDate);
        quiz.setDurationMinutes(durationMinutes);
        
        return quizRepository.save(quiz);
    }
}
