package com.college.attendance.config;

import com.college.attendance.model.Role;
import com.college.attendance.model.User;
import com.college.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminInitConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AdminInitConfig.class);

    @Bean
    public CommandLineRunner initAdminUser() {
        return args -> {
            // Check if admin already exists
            if (userRepository.findByUsername("admin_edutrack").isEmpty()) {
                logger.info("Creating admin user...");
                
                User adminUser = new User();
                adminUser.setUsername("admin_edutrack");
                adminUser.setPassword(passwordEncoder.encode("A9$k2pL8#xB7!fR3"));
                adminUser.setFullName("System Administrator");
                adminUser.setEmail("admin@edutrack.com");
                adminUser.setRole(Role.ADMIN);
                adminUser.setEmailVerified(true);
                
                userRepository.save(adminUser);
                
                logger.info("Admin user created successfully");
            } else {
                logger.info("Admin user already exists");
            }
        };
    }
} 