package com.college.attendance.controller;

import com.college.attendance.dto.JwtResponse;
import com.college.attendance.dto.LoginRequest;
import com.college.attendance.dto.RegisterRequest;
import com.college.attendance.dto.VerifyEmailDto;
import com.college.attendance.model.User;
import com.college.attendance.repository.UserRepository;
import com.college.attendance.security.CustomUserDetailsService;
import com.college.attendance.security.JwtTokenUtil;
import com.college.attendance.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            final UserDetails userDetails = userDetailsService
                .loadUserByUsername(loginRequest.getUsername());
            
            User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Check if email is verified
            if (!user.isEmailVerified()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email not verified. Please verify your email first."));
            }
            
            final String token = jwtTokenUtil.generateToken(userDetails);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Login successful",
                new JwtResponse(token, user.getUsername(), user.getFullName(), user.getEmail())
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Username is already taken"));
        }
        
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Email is already in use"));
        }
        
        // Generate a verification code
        String verificationCode = generateVerificationCode();
        
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        // Student ID is now optional and not included in registration
        user.setCourses(new HashSet<>());
        user.setEmailVerified(false);
        user.setVerificationCode(verificationCode);
        
        userRepository.save(user);
        
        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        
        return ResponseEntity.ok(ApiResponse.success(
            "User registered successfully. Please check your email for verification code.",
            "A verification code has been sent to " + user.getEmail()
        ));
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<JwtResponse>> verifyEmail(@Valid @RequestBody VerifyEmailDto verifyEmailDto) {
        User user = userRepository.findByEmail(verifyEmailDto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Email not found"));
        
        if (user.isEmailVerified()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Email is already verified"));
        }
        
        if (!user.getVerificationCode().equals(verifyEmailDto.getCode())) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Invalid verification code"));
        }
        
        // Verify email
        user.setEmailVerified(true);
        user.setVerificationCode(null); // Clear the code
        userRepository.save(user);
        
        // Generate JWT token
        final UserDetails userDetails = userDetailsService
            .loadUserByUsername(user.getUsername());
        
        final String token = jwtTokenUtil.generateToken(userDetails);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Email verified successfully",
            new JwtResponse(token, user.getUsername(), user.getFullName(), user.getEmail())
        ));
    }
    
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }
}