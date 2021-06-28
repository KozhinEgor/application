package com.application_tender.tender.subsidiaryModels;

import com.application_tender.tender.models.UserRole;

public class AuthenticationResponse {
    private final String token;
    private final String username;
    private final UserRole role;

    public AuthenticationResponse(String token, String username, UserRole role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}


