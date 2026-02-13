package com.restaurant.service;

import com.restaurant.dto.auth.AuthResponse;
import com.restaurant.dto.auth.LoginRequest;
import com.restaurant.dto.auth.RegisterRequest;
import com.restaurant.dto.auth.UserDto;
import com.restaurant.entity.User;
import com.restaurant.repository.UserRepository;
import com.restaurant.security.JwtTokenProvider;
import com.restaurant.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded_password")
                .fullName("Test User")
                .email("test@example.com")
                .phone("1234567890")
                .role(User.Role.CUSTOMER)
                .active(true)
                .build();
    }

    @Test
    void login_withValidCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        UserPrincipal principal = UserPrincipal.create(testUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals(1L, response.getUserId());
    }

    @Test
    void register_withNewUser_createsUserAndReturnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setFullName("New User");
        request.setEmail("new@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserPrincipal principal = UserPrincipal.create(testUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_withExistingUsername_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setFullName("Test");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withExistingEmail_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setFullName("Test");
        request.setEmail("test@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(request));
    }

    @Test
    void getCurrentUser_withValidId_returnsUserDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDto dto = authService.getCurrentUser(1L);

        assertNotNull(dto);
        assertEquals("testuser", dto.getUsername());
        assertEquals("Test User", dto.getFullName());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("CUSTOMER", dto.getRole());
    }

    @Test
    void getCurrentUser_withInvalidId_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.getCurrentUser(999L));
    }

    @Test
    void refreshToken_returnsNewToken() {
        UserPrincipal principal = UserPrincipal.create(testUser);
        when(tokenProvider.generateTokenForUser(principal)).thenReturn("new-jwt-token");

        AuthResponse response = authService.refreshToken(principal);

        assertEquals("new-jwt-token", response.getToken());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void updateProfile_updatesFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto dto = authService.updateProfile(1L, "Updated Name", "new@example.com", "9999999999");

        verify(userRepository).save(testUser);
        assertEquals("Updated Name", testUser.getFullName());
    }
}
