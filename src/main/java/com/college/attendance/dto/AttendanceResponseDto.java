package com.college.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponseDto {
    private Long id;
    private String studentName;
    private String studentId;
    private String courseCode;
    private String courseName;
    private String timestamp;
    private boolean verified;
    private String verificationMethod;

    public void setTimestamp(LocalDateTime timestamp) {
        // Convert UTC to Africa/Cairo timezone
        ZonedDateTime utc = timestamp.atZone(ZoneId.of("UTC"));
        ZonedDateTime cairoTime = utc.withZoneSameInstant(ZoneId.of("Africa/Cairo"));
        this.timestamp = cairoTime.toLocalDateTime().toString();
    }
}