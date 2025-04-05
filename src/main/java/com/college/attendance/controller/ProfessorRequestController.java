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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        
        // Remove authentication check - allow unauthenticated submissions
        
        // Check if email already exists
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("A user with this email already exists"));
        }

        // Check if there's already a pending request for this email
        Optional<ProfessorRequest> existingRequest = requestRepository.findByEmail(requestDto.getEmail());
        if (existingRequest.isPresent() && 
            existingRequest.get().getStatus() == RequestStatus.PENDING) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("A pending request for this email already exists"));
        }

        ProfessorRequest request = new ProfessorRequest();
        request.setFullName(requestDto.getFullName());
        request.setEmail(requestDto.getEmail());
        request.setIdImageUrl(requestDto.getIdImageUrl());
        request.setDepartment(requestDto.getDepartment());
        request.setAdditionalInfo(requestDto.getAdditionalInfo());
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(RequestStatus.PENDING);
        
        // No reviewer yet since this is just a submission
        
        ProfessorRequest savedRequest = requestRepository.save(request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Request submitted successfully", savedRequest)
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProfessorRequest>>> getPendingRequests() {
        List<ProfessorRequest> pendingRequests = 
            requestRepository.findByStatus(RequestStatus.PENDING);
        
        return ResponseEntity.ok(ApiResponse.success(pendingRequests));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProfessorRequest>>> getPendingRequestsAlternate() {
        List<ProfessorRequest> pendingRequests = 
            requestRepository.findByStatus(RequestStatus.PENDING);
        
        return ResponseEntity.ok(ApiResponse.success(pendingRequests));
    }

    @PutMapping("/{requestId}/review")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<ProfessorRequest>> reviewRequest(
            @PathVariable Long requestId, 
            @Valid @RequestBody ReviewRequestDto reviewDto) {
        
        ProfessorRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        
        if (request.getStatus() != RequestStatus.PENDING) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("This request has already been reviewed"));
        }
        
        // Get reviewer username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String reviewerUsername = authentication.getName();
        
        request.setReviewDate(LocalDateTime.now());
        request.setReviewedBy(reviewerUsername);
        
        if (reviewDto.getApproved()) {
            // Approve the request
            request.setStatus(RequestStatus.APPROVED);
            
            // Generate a random password for the new professor
            String password = generateRandomPassword();
            
            // Create a professor account
            User professor = new User();
            professor.setUsername(request.getEmail().split("@")[0]); // Use part of email as username
            professor.setPassword(passwordEncoder.encode(password));
            professor.setFullName(request.getFullName());
            professor.setEmail(request.getEmail());
            professor.setRole(Role.PROFESSOR);
            professor.setEmailVerified(true);
            professor.setProfessorRequest(request);
            
            userRepository.save(professor);
            
            // Send email with credentials
            try {
                emailService.sendProfessorRequestApprovalEmail(
                        request.getEmail(), 
                        password
                );
            } catch (Exception e) {
                // Log but continue, as the account has been created
                System.err.println("Failed to send approval email: " + e.getMessage());
            }
        } else {
            // Reject the request
            request.setStatus(RequestStatus.REJECTED);
            request.setRejectionReason(reviewDto.getRejectionReason());
            
            // Send rejection email
            try {
                emailService.sendProfessorRequestRejectionEmail(
                    request.getEmail(),
                    reviewDto.getRejectionReason()
                );
            } catch (Exception e) {
                // Log but continue
                System.err.println("Failed to send rejection email: " + e.getMessage());
            }
        }
        
        requestRepository.save(request);
        
        return ResponseEntity.ok(ApiResponse.success(
                reviewDto.getApproved() ? "Request approved successfully" : "Request rejected successfully",
                request
        ));
    }
    
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
} 