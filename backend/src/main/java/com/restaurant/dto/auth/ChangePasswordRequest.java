package com.restaurant.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String newPassword;
}
