package com.college.attendance.repository;

import com.college.attendance.model.ClassSession;
import com.college.attendance.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    
    /**
     * Find a verification code by its code value
     */
    Optional<VerificationCode> findByCode(String code);
    
    /**
     * Find all verification codes for a class session
     */
    List<VerificationCode> findByClassSession(ClassSession classSession);
    
    /**
     * Find all active verification codes (not expired and not used)
     */
    List<VerificationCode> findByExpiryTimeAfterAndUsedFalse(LocalDateTime now);
    
    /**
     * Find all active verification codes for a class session
     */
    List<VerificationCode> findByClassSessionAndExpiryTimeAfterAndUsedFalse(
            ClassSession classSession, LocalDateTime now);
} 