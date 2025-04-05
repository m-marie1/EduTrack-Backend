package com.college.attendance.model;

public enum Role {
    STUDENT("ROLE_STUDENT"),
    PROFESSOR("ROLE_PROFESSOR"),
    ADMIN("ROLE_ADMIN");
    
    private final String authority;
    
    Role(String authority) {
        this.authority = authority;
    }
    
    public String getAuthority() {
        return authority;
    }
    
    @Override
    public String toString() {
        return authority;
    }
} 