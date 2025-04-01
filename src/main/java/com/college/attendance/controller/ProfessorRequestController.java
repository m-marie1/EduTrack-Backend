package com.college.attendance.controller;

import com.college.attendance.dto.ProfessorRequestDto;
import com.college.attendance.dto.ReviewRequestDto;
import com.college.attendance.model.ProfessorRequest;
import com.college.attendance.model.RequestStatus;
import com.college.attendance.model.Role;
import com.college.attendance.model.User;
import com.college.attendance.repository.ProfessorRequestRepository;
import com.college.attendance.repository.UserRepository;
import com.college.attendance.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/professor-requests")
@RequiredArgsConstructor
public class ProfessorRequestController {
    
    private final ProfessorRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    @PostMapping
    public ResponseEntity<ApiResponse<ProfessorRequest>> submitRequest(
            @Valid @RequestBody ProfessorRequestDto requestDto) {
        
        // Check if email already exists in users
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email is already registered"));
        }
        
        // Check if there's already a pending request for this email
        Optional<ProfessorRequest> existingRequest = requestRepository.findByEmail(requestDto.getEmail());
        if (existingRequest.isPresent() && 
            existingRequest.get().getStatus() == RequestStatus.PENDING) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("A request for this email is already pending"));
        }
        
        ProfessorRequest request = new ProfessorRequest();
        request.setFullName(requestDto.getFullName());
        request.setEmail(requestDto.getEmail());
        request.setIdImageUrl(requestDto.getIdImageUrl());
        request.setDepartment(requestDto.getDepartment());
        request.setAdditionalInfo(requestDto.getAdditionalInfo());
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(RequestStatus.PENDING);
        
        ProfessorRequest savedRequest = requestRepository.save(request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Request submitted successfully", savedRequest)
        );
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProfessorRequest>>> getPendingRequests() {
        List<ProfessorRequest> pendingRequests = 
            requestRepository.findByStatus(RequestStatus.PENDING);
        
        return ResponseEntity.ok(ApiResponse.success(pendingRequests));
    }
    
    @PostMapping("/{requestId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> reviewRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody ReviewRequestDto reviewDto) {
        
        ProfessorRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (request.getStatus() != RequestStatus.PENDING) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("This request has already been processed"));
        }
        
        request.setReviewDate(LocalDateTime.now());
        request.setReviewedBy(reviewDto.getReviewedBy());
        
        if (reviewDto.getApproved()) {
            // Approve the request and create a professor account
            request.setStatus(RequestStatus.APPROVED);
            
            // Generate a random password
            String password = generateRandomPassword();
            
            User professor = new User();
            professor.setFullName(request.getFullName());
            professor.setEmail(request.getEmail());
            professor.setUsername(request.getEmail()); // Use email as username
            professor.setPassword(passwordEncoder.encode(password));
            professor.setRole(Role.PROFESSOR);
            professor.setEmailVerified(true); // Auto-verify for professors
            professor.setProfessorRequest(request);
            
            userRepository.save(professor);
            
            // Send approval email with login credentials
            emailService.sendProfessorRequestApprovalEmail(request.getEmail(), password);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Professor account created successfully",
                "Login credentials have been sent to the professor's email"
            ));
        } else {
            // Reject the request
            request.setStatus(RequestStatus.REJECTED);
            request.setRejectionReason(reviewDto.getRejectionReason());
            
            // Send rejection email
            emailService.sendProfessorRequestRejectionEmail(
                request.getEmail(), 
                reviewDto.getRejectionReason()
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "Request rejected",
                "A rejection email has been sent"
            ));
        }
    }
    
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
} 