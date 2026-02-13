package com.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String type;
    private String title;
    private String message;
    private Long referenceId;
    private String referenceType;
    private LocalDateTime timestamp;
}
