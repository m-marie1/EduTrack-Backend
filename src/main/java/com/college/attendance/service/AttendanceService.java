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
    
    String enrollUserInCourse(User user, Long courseId);

    /**
     * Retrieves a list of users (students) who recorded attendance for a specific course on a given date.
     * Only returns records marked as verified (i.e., code was correct).
     *
     * @param courseId The ID of the course.
     * @param date The specific date.
     * @return A list of UserDto representing the attendees.
     */
// Duplicate method signature removed
    List<com.college.attendance.dto.UserDto> getAttendeesForCourseOnDate(Long courseId, LocalDate date);

    /**
     * Generates a spreadsheet (e.g., CSV) containing the full attendance history for a course.
     *
     * @param courseId The ID of the course.
     * @return A byte array representing the spreadsheet file.
     */
    byte[] getCourseAttendanceSpreadsheet(Long courseId);
}