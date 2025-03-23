package com.college.attendance.dto;

import com.college.attendance.model.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    
    private Long id;
    private String courseCode;
    private String courseName;
    private String description;
    private LocalTime startTime;
    private LocalTime endTime;
    private Set<DayOfWeek> days;
    
    public static CourseDto fromEntity(Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setCourseCode(course.getCourseCode());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        dto.setStartTime(course.getStartTime());
        dto.setEndTime(course.getEndTime());
        dto.setDays(course.getDays());
        return dto;
    }
} 