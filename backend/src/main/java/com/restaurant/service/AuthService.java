package com.restaurant.service;

import com.restaurant.dto.auth.AuthResponse;
import com.restaurant.dto.auth.LoginRequest;
import com.restaurant.dto.auth.RegisterRequest;
import com.restaurant.dto.auth.UserDto;
import com.restaurant.entity.User;
import com.restaurant.repository.UserRepository;
import com.restaurant.security.JwtTokenProvider;
import com.restaurant.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return new AuthResponse(token, principal.getId(), principal.getUsername(), principal.getRole());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(User.Role.CUSTOMER)
                .build();

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = tokenProvider.generateToken(authentication);
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return UserDto.fromEntity(user);
    }

    public AuthResponse refreshToken(UserPrincipal principal) {
        String newToken = tokenProvider.generateTokenForUser(principal);
        return new AuthResponse(newToken, principal.getId(), principal.getUsername(), principal.getRole());
    }

    @Transactional
    public UserDto updateProfile(Long userId, String fullName, String email, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if (email != null && !email.isBlank()) {
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already in use");
            }
            user.setEmail(email);
        }
        if (phone != null) {
            user.setPhone(phone);
        }

        userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password by attempting authentication
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), currentPassword)
        );

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
