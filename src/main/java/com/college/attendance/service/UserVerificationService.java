package com.college.attendance.service;

import com.college.attendance.model.User;
import com.college.attendance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class UserVerificationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    // Store last code sent time for rate limiting
    private final Map<String, LocalDateTime> lastCodeSentTime = new ConcurrentHashMap<>();
    // Store failed verification attempts
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int RATE_LIMIT_SECONDS = 30;
    
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));
                
        checkRateLimit(email);
        
        String resetCode = generateVerificationCode();
        user.setResetCode(resetCode);
        user.setResetCodeExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        
        emailService.sendPasswordResetEmail(email, resetCode);
        lastCodeSentTime.put(email, LocalDateTime.now());
    }
    
    public void resetPassword(String email, String resetCode, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));
                
        if (user.getResetCode() == null || user.getResetCodeExpiry() == null || 
            LocalDateTime.now().isAfter(user.getResetCodeExpiry())) {
            throw new IllegalStateException("Reset code has expired. Please request a new one.");
        }
        
        if (!user.getResetCode().equals(resetCode)) {
            incrementFailedAttempts(email);
            throw new IllegalArgumentException("Invalid reset code");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        failedAttempts.remove(email); // Reset failed attempts on success
        userRepository.save(user);
    }
    
    public void verifyResetCode(String email, String resetCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));
                
        if (user.getResetCode() == null || user.getResetCodeExpiry() == null || 
            LocalDateTime.now().isAfter(user.getResetCodeExpiry())) {
            throw new IllegalStateException("Reset code has expired. Please request a new one.");
        }
        
        if (!user.getResetCode().equals(resetCode)) {
            incrementFailedAttempts(email);
            throw new IllegalArgumentException("Invalid reset code");
        }
        
        // Reset code is valid, but don't clear it yet since it will be needed for actual password reset
    }
    
    public void resendVerificationCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));
                
        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email is already verified");
        }
        
        checkRateLimit(email);
        
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        userRepository.save(user);
        
        emailService.sendVerificationEmail(email, verificationCode);
        lastCodeSentTime.put(email, LocalDateTime.now());
    }
    
    private void checkRateLimit(String email) {
        LocalDateTime lastSent = lastCodeSentTime.get(email);
        if (lastSent != null && 
            LocalDateTime.now().isBefore(lastSent.plusSeconds(RATE_LIMIT_SECONDS))) {
            throw new IllegalStateException(
                String.format("Please wait %d seconds before requesting another code", 
                    RATE_LIMIT_SECONDS));
        }
    }
    
    private void incrementFailedAttempts(String email) {
        int attempts = failedAttempts.getOrDefault(email, 0) + 1;
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            throw new IllegalStateException("Too many failed attempts. Please wait or request a new code.");
        }
        failedAttempts.put(email, attempts);
    }
    
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }
}