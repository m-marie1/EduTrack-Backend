package com.college.attendance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Profile("!dev") // Use in production, not in dev environment
public class EmailServiceImpl implements EmailService {
    
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Override
    public void sendVerificationEmail(String to, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Email Verification");
        message.setText("Your verification code is: " + verificationCode + 
                       "\n\nPlease use this code to verify your email address.");
        
        try {
            log.info("Attempting to send verification email to: {}", to);
            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
    
    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Password Reset");
        message.setText("Your password reset token is: " + resetToken + 
                       "\n\nPlease use this token to reset your password.");
        
        try {
            log.info("Attempting to send password reset email to: {}", to);
            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
    
    @Override
    public void sendProfessorRequestApprovalEmail(String to, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Professor Account Request Approved");
        message.setText("Your request for a professor account has been approved!" +
                       "\n\nYou can now log in using your email and the following temporary password: " + 
                       password + 
                       "\n\nPlease change your password after your first login.");
        
        try {
            log.info("Attempting to send professor approval email to: {}", to);
            mailSender.send(message);
            log.info("Professor approval email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send professor approval email to: {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send professor approval email", e);
        }
    }
    
    @Override
    public void sendProfessorRequestRejectionEmail(String to, String reason) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Professor Account Request Status");
        message.setText("Your request for a professor account could not be approved at this time." +
                       "\n\nReason: " + reason +
                       "\n\nFeel free to submit a new request or contact support for more information.");
        
        try {
            log.info("Attempting to send professor rejection email to: {}", to);
            mailSender.send(message);
            log.info("Professor rejection email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send professor rejection email to: {}. Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send professor rejection email", e);
        }
    }
} 