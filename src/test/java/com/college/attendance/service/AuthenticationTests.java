package com.college.attendance.service;

import com.college.attendance.controller.AuthController;
import com.college.attendance.controller.ApiResponse; // Assuming ApiResponse is in this package
import com.college.attendance.dto.JwtResponse;
import com.college.attendance.dto.LoginRequest;
import com.college.attendance.dto.RegisterRequest;
import com.college.attendance.dto.VerifyEmailDto;
import com.college.attendance.model.Role;
import com.college.attendance.model.User;
import com.college.attendance.repository.UserRepository;
import com.college.attendance.security.CustomUserDetailsService;
import com.college.attendance.security.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AuthController.class) // Test only the AuthController layer
public class AuthenticationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager; // Mocked by Spring Security Test

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private EmailService emailService;

    @MockBean
    private PasswordEncoder passwordEncoder; // Need to mock this as it's used in AuthController

    @MockBean
    private CustomUserDetailsService userDetailsService; // Mocked by Spring Security Test

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private VerifyEmailDto verifyEmailDto;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        // Need to encode password for UserDetails, but PasswordEncoder mock handles the controller's use
        testUser.setPassword("encodedPassword"); // Assume this is encoded
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setRole(Role.STUDENT);
        testUser.setEmailVerified(false);
        testUser.setVerificationCode("123456");

        // Setup register request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password"); // Raw password
        registerRequest.setEmail("test@example.com");
        registerRequest.setFullName("Test User");

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        // Setup verify email dto
        verifyEmailDto = new VerifyEmailDto();
        verifyEmailDto.setEmail("test@example.com");
        verifyEmailDto.setCode("123456");

        // Setup user details for service/token generation
        userDetails = new org.springframework.security.core.userdetails.User(
            testUser.getUsername(),
            testUser.getPassword(), // Encoded password needed here
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + testUser.getRole().name()))
        );

        // --- Setup Mocks ---
        // We mock interactions AuthController depends on
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtTokenUtil.generateToken(any(UserDetails.class))).thenReturn("test-jwt-token");
        // Mock password encoding as AuthController uses it
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        // Mock saving user for registration
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L); // Simulate ID generation
            return savedUser;
        });
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        // Setup: Assume username and email are available
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message", containsString("User registered successfully")));

        // Verify email was sent and user saved
        verify(emailService, times(1)).sendVerificationEmail(eq(registerRequest.getEmail()), anyString());
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(registerRequest.getUsername(), savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword()); // Verify encoded password
        assertEquals(registerRequest.getEmail(), savedUser.getEmail());
        assertEquals(registerRequest.getFullName(), savedUser.getFullName());
        assertFalse(savedUser.isEmailVerified());
        assertNotNull(savedUser.getVerificationCode());
    }

    @Test
    void testRegisterUser_UsernameTaken() throws Exception {
        // Setup: Username exists
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message", containsString("Username is already taken")));

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }
    
    @Test
    void testRegisterUser_EmailTaken() throws Exception {
        // Setup: Email exists
        when(userRepository.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message", containsString("Email is already in use")));

        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }


    @Test
    void testVerifyEmail_Success() throws Exception {
        // Setup: User exists, not verified, correct code
        testUser.setEmailVerified(false);
        testUser.setVerificationCode("123456");
        when(userRepository.findByEmail(verifyEmailDto.getEmail())).thenReturn(Optional.of(testUser));
        // Mock loadUserByUsername called after verification
        when(userDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(userDetails);


        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyEmailDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message", containsString("Email verified successfully")))
            .andExpect(jsonPath("$.data.token").value("test-jwt-token"));

        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertTrue(savedUser.isEmailVerified());
        assertNull(savedUser.getVerificationCode());
        verify(jwtTokenUtil, times(1)).generateToken(userDetails);
    }

    @Test
    void testVerifyEmail_AlreadyVerified() throws Exception {
        testUser.setEmailVerified(true);
        when(userRepository.findByEmail(verifyEmailDto.getEmail())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyEmailDto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message", containsString("Email is already verified")));

        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    @Test
    void testVerifyEmail_InvalidCode() throws Exception {
        testUser.setEmailVerified(false);
        testUser.setVerificationCode("wrongcode"); // Different code
        when(userRepository.findByEmail(verifyEmailDto.getEmail())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyEmailDto))) // DTO has '123456'
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message", containsString("Invalid verification code")));

        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenUtil, never()).generateToken(any());
    }


    @Test
    void testLogin_Success() throws Exception {
        // Setup: User exists and is verified
        testUser.setEmailVerified(true);
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        // Mock successful authentication by AuthenticationManager
        // No need to explicitly mock `authenticate` if using @MockBean AuthenticationManager,
        // Spring Security Test handles this often, but being explicit is safer.
        // We'll assume it passes if no exception is thrown.

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message", containsString("Login successful")))
            .andExpect(jsonPath("$.data.token").value("test-jwt-token"))
            .andExpect(jsonPath("$.data.username").value(testUser.getUsername()));

        // Verify token was generated after successful authentication steps
        verify(jwtTokenUtil, times(1)).generateToken(userDetails);
    }

    @Test
    void testLogin_FailsWhenEmailNotVerified() throws Exception {
        // Setup: User exists but is NOT verified
        testUser.setEmailVerified(false);
        when(userRepository.findByUsername(loginRequest.getUsername())).thenReturn(Optional.of(testUser));
        // Assume authentication manager passes initially (checks credentials only)
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isBadRequest()) // Should fail due to email verification check
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message", containsString("Email not verified")));

        // Verify token was NOT generated
        verify(jwtTokenUtil, never()).generateToken(any());
    }

    @Test
    void testLogin_FailsWithBadCredentials() throws Exception {
        // Setup: Mock AuthenticationManager to throw BadCredentialsException
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isBadRequest()) // AuthController catches and returns bad request
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message", containsString("Invalid username or password")));

        // Verify token was NOT generated
        verify(jwtTokenUtil, never()).generateToken(any());
    }
} 