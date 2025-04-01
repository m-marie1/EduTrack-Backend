package com.college.attendance.service;

public interface EmailService {
    
    void sendVerificationEmail(String to, String verificationCode);
    
    void sendPasswordResetEmail(String to, String resetToken);
    
    void sendProfessorRequestApprovalEmail(String to, String password);
    
    void sendProfessorRequestRejectionEmail(String to, String reason);
} 