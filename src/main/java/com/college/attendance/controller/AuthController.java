package com.college.attendance.controller;

import com.college.attendance.dto.JwtResponse;
import com.college.attendance.dto.LoginRequest;
import com.college.attendance.dto.RegisterRequest;
import com.college.attendance.dto.VerifyEmailDto;
import com.college.attendance.dto.ChangePasswordRequest;
import com.college.attendance.dto.ForgotPasswordRequest;
import com.college.attendance.dto.ResetPasswordRequest;
import com.college.attendance.dto.VerifyResetCodeRequest;
import com.college.attendance.exception.ResourceNotFoundException;
import com.college.attendance.model.User;
import com.college.attendance.repository.UserRepository;
import com.college.attendance.security.CustomUserDetailsService;
import com.college.attendance.security.JwtTokenUtil;
import com.college.attendance.service.EmailService;
import com.college.attendance.service.UserVerificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    private final UserVerificationService userVerificationService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String username;
            
            // Check if email is provided instead of username
            if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()) {
                if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty()) {
                    return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Either username or email must be provided"));
                }
                
                // Find user by email
                User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
                
                username = user.getUsername();
            } else {
                username = loginRequest.getUsername();
            }
            
            // Authenticate with username
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    username,
                    loginRequest.getPassword()
                )
            );
            
            final UserDetails userDetails = userDetailsService
                .loadUserByUsername(username);
            
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Check if email is verified
            if (!user.isEmailVerified()) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email not verified. Please verify your email first."));
            }
            
            final String token = jwtTokenUtil.generateToken(userDetails);
            
            // Log user details and token for debugging
            logger.info("User logged in: username={}, role={}", user.getUsername(), user.getRole());
            logger.info("Generated token: {}", token);
            
            // Create response with user info including role
            JwtResponse response = new JwtResponse(
                token, 
                user.getUsername(), 
                user.getFullName(), 
                user.getEmail(),
                user.getRole().toString()
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "Login successful",
                response
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Invalid credentials"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@Valid @RequestBody RegisterRequest registerRequest) {
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
        
        // IMPORTANT: Log the verification code for troubleshooting in production
        // This should be removed in a real production environment once email sending is confirmed working
        logger.info("DEBUG_VERIFICATION_CODE: User {} with email {} has verification code: {}", 
            user.getUsername(), user.getEmail(), verificationCode);
        
        Map<String, String> responseData = new HashMap<>();
        responseData.put("email", user.getEmail());
        // Expose verification code in the response for testing purposes
        // IMPORTANT: This should be removed in a production environment once email sending is confirmed working
        responseData.put("verificationCode", verificationCode);
        responseData.put("message", "A verification code has been sent to " + user.getEmail() + 
            ". If you don't receive it, check logs or contact support.");
        
        try {
            // Send verification email
            emailService.sendVerificationEmail(user.getEmail(), verificationCode);
            logger.info("Verification email successfully triggered for user: {}", user.getUsername());
        } catch (Exception e) {
            logger.error("Failed to send verification email to user: {}. Error: {}", 
                user.getUsername(), e.getMessage(), e);
            responseData.put("emailStatus", "Email sending failed, but you can still verify using the verification code");
        }
        
        return ResponseEntity.ok(ApiResponse.success(
            "User registered successfully. Please check your email for verification code.",
            responseData
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
        
        // Log verification and role for debugging
        logger.info("Email verified for user: {} with role: {}", user.getUsername(), user.getRole());
        
        // Create response with user info including role
        JwtResponse response = new JwtResponse(
            token, 
            user.getUsername(), 
            user.getFullName(), 
            user.getEmail(),
            user.getRole().toString()
        );
        
        return ResponseEntity.ok(ApiResponse.success(
            "Email verified successfully",
            response
        ));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        try {
            userVerificationService.changePassword(user, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            userVerificationService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(
                "Password reset instructions have been sent to your email",
                null
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            userVerificationService.resetPassword(request.getEmail(), request.getResetCode(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success(
                "Password has been reset successfully",
                null
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerificationCode(@RequestBody ForgotPasswordRequest request) {
        try {
            userVerificationService.resendVerificationCode(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success(
                "Verification code has been resent to your email",
                null
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<ApiResponse<Void>> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        try {
            userVerificationService.verifyResetCode(request.getEmail(), request.getResetCode());
            return ResponseEntity.ok(ApiResponse.success("Reset code verified successfully", null));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6-digit code
        return String.valueOf(code);
    }
}