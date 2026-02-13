package com.restaurant.dto.auth;

import com.restaurant.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private Boolean active;
    private String createdAt;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .active(user.getActive())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)
                .build();
    }
}
