package com.college.attendance.security;

import com.college.attendance.config.TestConfig;
import com.college.attendance.model.Role;
import com.college.attendance.model.User;
import com.college.attendance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import(TestConfig.class)
public class JwtSecurityTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<String> tokenCaptor;

    private User testUser;
    private UserDetails userDetails;
    private String validToken = "valid.jwt.token";
    private JwtRequestFilter jwtRequestFilter;

    @BeforeEach
    void setUp() {
        // Clear the security context before each test
        SecurityContextHolder.clearContext();
        
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        // Optional studentId for legacy compatibility  
        testUser.setStudentId("12345");
        testUser.setRole(Role.STUDENT);
        testUser.setEmailVerified(true);
        
        // Setup mocks
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtTokenUtil.extractUsername(validToken)).thenReturn("testuser");
        
        // Mock userDetails
        userDetails = org.springframework.security.core.userdetails.User.builder()
            .username("testuser")
            .password(testUser.getPassword())
            .authorities("ROLE_STUDENT")
            .build();
            
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtTokenUtil.validateToken(eq(validToken), any(UserDetails.class))).thenReturn(true);
        
        // Create the filter
        jwtRequestFilter = new JwtRequestFilter(userDetailsService, jwtTokenUtil, userRepository);
    }

    @Test
    void testValidJwtTokenAuthentication() throws ServletException, IOException {
        // Setup
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        
        // Test
        jwtRequestFilter.doFilterInternal(request, response, filterChain);
        
        // Verify
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("testuser", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT")));
    }

    @Test
    void testInvalidJwtTokenFormat() throws ServletException, IOException {
        // Setup
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "InvalidFormat " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        
        // Test
        jwtRequestFilter.doFilterInternal(request, response, filterChain);
        
        // Verify
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }

    @Test
    void testNoJwtToken() throws ServletException, IOException {
        // Setup
        MockHttpServletRequest request = new MockHttpServletRequest();
        // No Authorization header
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        
        // Test
        jwtRequestFilter.doFilterInternal(request, response, filterChain);
        
        // Verify
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }
    
    @Test
    void testUserNotFoundInDatabase() throws ServletException, IOException {
        // Setup
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + validToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        
        // Mock user not found in database
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        
        // Test
        jwtRequestFilter.doFilterInternal(request, response, filterChain);
        
        // Verify
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }
    
    @Test
    void testInvalidToken() throws ServletException, IOException {
        // Setup
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();
        
        // Mock token validation failure
        when(jwtTokenUtil.extractUsername("invalid.token")).thenReturn("testuser");
        when(jwtTokenUtil.validateToken(eq("invalid.token"), any(UserDetails.class))).thenReturn(false);
        
        // Test
        jwtRequestFilter.doFilterInternal(request, response, filterChain);
        
        // Verify
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNull(authentication);
    }
} 