package com.example.qlsv.application.dto.response;

import lombok.Data;

@Data
public class JwtAuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String role; // (ROLE_STUDENT, ROLE_LECTURER...)

    public JwtAuthResponse(String accessToken, Long userId, String username, String role) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
}