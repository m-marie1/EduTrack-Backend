package com.college.attendance.controller;

import com.college.attendance.dto.JwtResponse;
import com.college.attendance.dto.LoginRequest;
import com.college.attendance.dto.RegisterRequest;
import com.college.attendance.model.User;
import com.college.attendance.repository.UserRepository;
import com.college.attendance.security.CustomUserDetailsService;
import com.college.attendance.security.JwtTokenUtil;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
            
            final String token = jwtTokenUtil.generateToken(userDetails);
            
            User user = userRepository.findByUsername(loginRequest.getUsername()).get();
            
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
    public ResponseEntity<ApiResponse<JwtResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
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
        
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setStudentId(registerRequest.getStudentId());
        user.setCourses(new HashSet<>());
        
        userRepository.save(user);
        
        final UserDetails userDetails = userDetailsService
            .loadUserByUsername(registerRequest.getUsername());
        
        final String token = jwtTokenUtil.generateToken(userDetails);
        
        return ResponseEntity.ok(ApiResponse.success(
            "User registered successfully",
            new JwtResponse(token, user.getUsername(), user.getFullName(), user.getEmail())
        ));
    }
}