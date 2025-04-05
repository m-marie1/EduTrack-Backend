package com.college.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    
    private String token;
    private String username;
    private String fullName;
    private String email;
    private String role;
    
    // Constructor without role for backward compatibility
    public JwtResponse(String token, String username, String fullName, String email) {
        this.token = token;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = null;
    }
}