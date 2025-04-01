package com.college.attendance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class MockEmailServiceImpl implements EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(MockEmailServiceImpl.class);
    
    @Override
    public void sendVerificationEmail(String to, String verificationCode) {
        logger.info("MOCK EMAIL: Verification code for {} is: {}", to, verificationCode);
    }
    
    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        logger.info("MOCK EMAIL: Password reset token for {} is: {}", to, resetToken);
    }
    
    @Override
    public void sendProfessorRequestApprovalEmail(String to, String password) {
        logger.info("MOCK EMAIL: Professor request approved for {}. Temporary password: {}", to, password);
    }
    
    @Override
    public void sendProfessorRequestRejectionEmail(String to, String reason) {
        logger.info("MOCK EMAIL: Professor request rejected for {}. Reason: {}", to, reason);
    }
} 