package com.college.attendance.service;

import com.college.attendance.dto.AttendanceRecordDto;
import com.college.attendance.dto.AttendanceResponseDto;
import com.college.attendance.model.User;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    
    AttendanceResponseDto recordAttendance(User user, AttendanceRecordDto attendanceDto);
    
    List<AttendanceResponseDto> getUserAttendanceForCourse(Long userId, Long courseId);
    
    List<AttendanceResponseDto> getUserAttendanceForDate(Long userId, LocalDate date);
    
    List<AttendanceResponseDto> getCourseAttendanceForDate(Long courseId, LocalDate date);
    
    boolean isUserPresentForCourseToday(Long userId, Long courseId);
}