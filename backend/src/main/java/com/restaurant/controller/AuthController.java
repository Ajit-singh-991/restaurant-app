package com.restaurant.controller;

import com.restaurant.dto.auth.*;
import com.restaurant.security.UserPrincipal;
import com.restaurant.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.refreshToken(principal));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal.getId()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> updates) {
        return ResponseEntity.ok(authService.updateProfile(
                principal.getId(),
                updates.get("fullName"),
                updates.get("email"),
                updates.get("phone")
        ));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody @Valid ChangePasswordRequest request) {
        authService.changePassword(principal.getId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
