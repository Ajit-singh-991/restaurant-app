package com.restaurant.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String type;
    private Long userId;
    private String username;
    private String role;

    public AuthResponse(String token, Long userId, String username, String role) {
        this.token = token;
        this.type = "Bearer";
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
}
