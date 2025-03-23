package com.college.attendance.controller;

import com.college.attendance.dto.CourseDto;
import com.college.attendance.model.Course;
import com.college.attendance.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseRepository courseRepository;
    
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
}