package com.college.attendance.config;

import com.college.attendance.model.Course;
import com.college.attendance.model.User;
import com.college.attendance.repository.CourseRepository;
import com.college.attendance.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataLoader {

    @Bean
    @Profile("dev")
    public CommandLineRunner loadData(
            UserRepository userRepository,
            CourseRepository courseRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // Create test courses
            Course javaCourse = new Course();
            javaCourse.setCourseCode("CS101");
            javaCourse.setCourseName("Introduction to Java");
            javaCourse.setDescription("Fundamentals of Java programming");
            javaCourse.setStartTime(LocalTime.of(9, 0));
            javaCourse.setEndTime(LocalTime.of(11, 0));

            Set<DayOfWeek> javaDays = new HashSet<>();
            javaDays.add(DayOfWeek.MONDAY);
            javaDays.add(DayOfWeek.WEDNESDAY);
            javaCourse.setDays(javaDays);

            Course webCourse = new Course();
            webCourse.setCourseCode("CS102");
            webCourse.setCourseName("Web Development");
            webCourse.setDescription("HTML, CSS, and JavaScript");
            webCourse.setStartTime(LocalTime.of(13, 0));
            webCourse.setEndTime(LocalTime.of(15, 0));

            Set<DayOfWeek> webDays = new HashSet<>();
            webDays.add(DayOfWeek.TUESDAY);
            webDays.add(DayOfWeek.THURSDAY);
            webCourse.setDays(webDays);

            courseRepository.save(javaCourse);
            courseRepository.save(webCourse);

            // Create test user
            User testUser = new User();
            testUser.setUsername("student1");
            testUser.setPassword(passwordEncoder.encode("password"));
            testUser.setFullName("Test Student");
            testUser.setEmail("student1@college.edu");
            testUser.setStudentId("S12345");

            Set<Course> courses = new HashSet<>();
            courses.add(javaCourse);
            courses.add(webCourse);
            testUser.setCourses(courses);

            userRepository.save(testUser);

            System.out.println("Sample data loaded successfully!");
            System.out.println("Test User: student1/password");
            System.out.println("Courses: CS101, CS102");
        };
    }
}