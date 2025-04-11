package com.college.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // Added import

@SpringBootApplication
@EnableScheduling // Enable scheduled tasks
public class AttendanceSystemApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AttendanceSystemApplication.class, args);
    }
}