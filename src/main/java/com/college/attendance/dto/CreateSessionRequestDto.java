package com.college.attendance.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequestDto {

    @NotNull(message = "Course ID cannot be null")
    private Long courseId;

    @NotNull(message = "Expiry minutes cannot be null")
    @Min(value = 1, message = "Expiry time must be at least 1 minute")
    private Integer expiryMinutes; // Using Integer to allow @NotNull check
}