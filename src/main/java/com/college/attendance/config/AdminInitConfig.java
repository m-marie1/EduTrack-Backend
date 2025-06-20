package com.college.attendance.config;

import com.college.attendance.model.Role;
import com.college.attendance.model.User;
import com.college.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

@Component
public class AdminInitConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private boolean alreadySetup = false;

    @Autowired
    public AdminInitConfig(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------------------------------------------------------------------
    // Admin credentials injected from configuration / environment variables
    // ---------------------------------------------------------------------

    @Value("${ADMIN_USERNAME}")
    private String adminUsernameProp;

    @Value("${ADMIN_PASSWORD}")
    private String adminPasswordProp;

    @Value("${ADMIN_EMAIL}")
    private String adminEmailProp;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup) {
            return;
        }

        // Username, password and email for admin user are read from environment variables (or application properties)
        // with sensible defaults to preserve current behaviour in non-production environments.
        String adminUsername = adminUsernameProp;
        String adminPassword = adminPasswordProp;
        String adminEmail = adminEmailProp;

        // Check if admin already exists
        Optional<User> existingAdmin = userRepository.findByUsername(adminUsername);
        if (existingAdmin.isPresent()) {
            System.out.println("Admin user already exists, skipping creation");
            alreadySetup = true;
            return;
        }

        // Create admin user if it doesn't exist
        User adminUser = new User();
        adminUser.setUsername(adminUsername);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setFullName("System Administrator");
        adminUser.setEmail(adminEmail);
        adminUser.setRole(Role.ADMIN);
        adminUser.setEmailVerified(true);

        userRepository.save(adminUser);
        System.out.println("Created admin user: " + adminUsername);
        
        alreadySetup = true;
    }
} 