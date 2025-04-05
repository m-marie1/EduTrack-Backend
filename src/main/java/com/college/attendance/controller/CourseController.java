package com.college.attendance.controller;

import com.college.attendance.dto.CourseDto;
import com.college.attendance.model.Course;
import com.college.attendance.model.User;
import com.college.attendance.repository.CourseRepository;
import com.college.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseDto>>> getAllCourses() {
        List<CourseDto> courses = courseRepository.findAll().stream()
            .map(CourseDto::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(courses));
    }
    
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getCurrentCourses() {
        // Get the current day and time
        DayOfWeek today = DayOfWeek.from(java.time.LocalDate.now());
        LocalTime now = LocalTime.now();
        
        // Find courses that are in session right now
        List<CourseDto> currentCourses = courseRepository
            .findByDaysContainingAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(today, now, now)
            .stream()
            .map(CourseDto::fromEntity)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(ApiResponse.success(currentCourses));
    }
    
    @GetMapping("/enrolled")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getEnrolledCourses() {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get courses the user is enrolled in
        List<CourseDto> enrolledCourses = user.getCourses().stream()
            .map(CourseDto::fromEntity)
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(ApiResponse.success(enrolledCourses));
    }
    
    @PostMapping("/sample")
    public ResponseEntity<ApiResponse<String>> createSampleCourses() {
        // Check if we already have courses
        if (courseRepository.count() > 0) {
            return ResponseEntity.ok(ApiResponse.success("Sample courses already exist"));
        }
        
        // Create sample courses that are available all days for easy testing
        List<Course> sampleCourses = new ArrayList<>();
        
        // Create a set with all days of the week for testing
        Set<DayOfWeek> allDays = new HashSet<>(Arrays.asList(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        ));
        
        // Programming course - early morning
        Course programmingCourse = new Course();
        programmingCourse.setCourseCode("CS101");
        programmingCourse.setCourseName("Introduction to Programming");
        programmingCourse.setDescription("Learn the basics of programming with Java");
        programmingCourse.setStartTime(LocalTime.of(8, 0));
        programmingCourse.setEndTime(LocalTime.of(23, 59));
        programmingCourse.setDays(allDays);
        sampleCourses.add(programmingCourse);
        
        // Database course - mid-day
        Course databaseCourse = new Course();
        databaseCourse.setCourseCode("CS201");
        databaseCourse.setCourseName("Database Systems");
        databaseCourse.setDescription("Introduction to database design and SQL");
        databaseCourse.setStartTime(LocalTime.of(8, 0));
        databaseCourse.setEndTime(LocalTime.of(23, 59));
        databaseCourse.setDays(allDays);
        sampleCourses.add(databaseCourse);
        
        // Web Development course - afternoon
        Course webDevCourse = new Course();
        webDevCourse.setCourseCode("CS301");
        webDevCourse.setCourseName("Web Development");
        webDevCourse.setDescription("Learn to build modern web applications");
        webDevCourse.setStartTime(LocalTime.of(8, 0));
        webDevCourse.setEndTime(LocalTime.of(23, 59));
        webDevCourse.setDays(allDays);
        sampleCourses.add(webDevCourse);
        
        // Mobile App Development - late afternoon
        Course mobileDevCourse = new Course();
        mobileDevCourse.setCourseCode("CS401");
        mobileDevCourse.setCourseName("Mobile App Development");
        mobileDevCourse.setDescription("Develop applications for Android and iOS");
        mobileDevCourse.setStartTime(LocalTime.of(8, 0));
        mobileDevCourse.setEndTime(LocalTime.of(23, 59));
        mobileDevCourse.setDays(allDays);
        sampleCourses.add(mobileDevCourse);
        
        // AI and Machine Learning - evening
        Course aiCourse = new Course();
        aiCourse.setCourseCode("CS501");
        aiCourse.setCourseName("AI and Machine Learning");
        aiCourse.setDescription("Introduction to artificial intelligence and machine learning concepts");
        aiCourse.setStartTime(LocalTime.of(8, 0));
        aiCourse.setEndTime(LocalTime.of(23, 59));
        aiCourse.setDays(allDays);
        sampleCourses.add(aiCourse);
        
        // Save all courses
        courseRepository.saveAll(sampleCourses);
        
        return ResponseEntity.ok(ApiResponse.success("Created 5 sample courses"));
    }
}