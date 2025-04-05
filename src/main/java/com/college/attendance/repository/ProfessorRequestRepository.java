package com.college.attendance.repository;

import com.college.attendance.model.ProfessorRequest;
import com.college.attendance.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessorRequestRepository extends JpaRepository<ProfessorRequest, Long> {
    Optional<ProfessorRequest> findByEmail(String email);
    
    List<ProfessorRequest> findByStatus(RequestStatus status);
    
    Optional<ProfessorRequest> findByEmailAndStatus(String email, RequestStatus status);
} 